/*
Copyright 2011 Trampus Richmond. All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are
permitted provided that the following conditions are met:

   1. Redistributions of source code must retain the above copyright notice, this list of
      conditions and the following disclaimer.

   2. Redistributions in binary form must reproduce the above copyright notice, this list
      of conditions and the following disclaimer in the documentation and/or other materials
      provided with the distribution.

THIS SOFTWARE IS PROVIDED BY TRAMPUS RICHMOND ``AS IS'' AND ANY EXPRESS OR IMPLIED
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
TRAMPUS RICHMOND OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those of the authors and 
should not be interpreted as representing official policies, either expressed or implied, of Trampus Richmond.
 
 */

package voodoodriver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

public class SodaReporter {
	
	private String resultDir = "";
	private String reportLog = null;
	private FileOutputStream reportFD = null;
	private int Blocked = 0;
	private int Exceptions = 0;
	private int FailedAsserts = 0;
	private int PassedAsserts = 0;
	private int OtherErrors = 0;
	private int WatchDog = 0;
	private String LineSeparator = null;
	private boolean saveHTML = false;
	private int SavePageNum = 0;
	private SodaBrowser browser = null;
	private String CurrentMD5 = "";
	private String LastSavedPage = "";
	
	public SodaReporter(String reportName, String resultDir) {
		DateFormat fd = new SimpleDateFormat("MM-d-yyyy-hh-m-s-S");
		Date now = new Date();
		String date_str = fd.format(now);
		
		this.LineSeparator = System.getProperty("line.separator");
		
		if (resultDir != null) {
			File dir = new File(resultDir);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			
			this.resultDir = resultDir;
		} else {
			this.resultDir = System.getProperty("user.dir");
		}

		reportLog = this.resultDir + "/" + reportName + "-" + date_str + ".log";
		reportLog = FilenameUtils.separatorsToSystem(reportLog);
		System.out.printf("ReportFile: %s\n", reportLog);
		
		try {
			reportFD = new FileOutputStream(reportLog);
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}
	
	public void setSaveHTML(boolean setting, SodaBrowser browser) {
		this.saveHTML = setting;
		this.browser = browser;
	}
	
	public SodaTestResults getResults() {
		SodaTestResults result = null;
		Integer res = 0;
		
		result = new SodaTestResults();
		result.put("blocked", this.Blocked);
		result.put("exceptions", this.Exceptions);
		result.put("failedasserts", this.FailedAsserts);
		result.put("passedasserts", this.PassedAsserts);
		result.put("watchdog", this.WatchDog);
		result.put("errors", this.OtherErrors);
		
		if (this.Blocked > 0 || this.Exceptions > 0 || this.FailedAsserts > 0 || this.OtherErrors > 0) {
			res = -1;
		}
		
		result.put("result", res);
		
		return result;
	}
	
	private String replaceLineFeed(String str) {
		str = str.replaceAll("\n", "\\\\n");
		return str;
	}
	
	private void _log(String msg) {
		Date now = new Date();
		DateFormat df = new SimpleDateFormat("MM/d/yyyy-hh:m:s.S");
		String date_str = df.format(now);
		msg = replaceLineFeed(msg);
		String logstr = "[" + date_str + "]" + msg + this.LineSeparator;
		
		if (msg.isEmpty()) {
			msg = "Found empty message!";
		}
		
		try {
			this.reportFD.write(logstr.getBytes());
			System.out.printf("%s\n", msg);
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}
	
	public void closeLog() {
		try {
			this.reportFD.close();
			this.reportFD = null;
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	public void Log(String msg) {
		this._log("(*)" + msg);
	}
	
	public void Warn(String msg) {
		this._log("(W)" + msg);
	}
	
	public void ReportError(String msg) {
		this._log(String.format("(!)%s", msg));
		this.OtherErrors += 1;
	}
	
	public void ReportWatchDog() {
		this.WatchDog = 1;
	}
	
	public void ReportBlocked() {
		this.Blocked = 1;
	}
	
	/*
	 * ReportException -- Method
	 * 	This method formats a java exception class into a SODA log entry.  Both the message and the stack
	 * 	trace are reformatted and printed to the SODA log file, and the console.
	 * 
	 * Input:
	 * 	e: The exception that happened.
	 * 
	 * Output:
	 * 	None.
	 * 
	 */
	public void ReportException(Exception e) {
		this.Exceptions += 1;
		String msg = "--Exception Backtrace: ";
		StackTraceElement[] trace = e.getStackTrace();
		String message = "";
		
		if (e.getMessage() != null) {
			String[] msg_lines = e.getMessage().split("\\n");
			for (int i = 0; i <= msg_lines.length -1; i++) {
				message += msg_lines[i] + "  ";
			}
			
			this._log("(!)Exception raised: " + message);
			
			for (int i = 0; i <= trace.length -1; i++) {
				String tmp = trace[i].toString();
				msg += "--" + tmp;
			}
		} else {
			msg = "Something really bad happened here and the exception is null!!!";
			e.printStackTrace();
		}
		
		this._log("(!)" + msg);
		this.SavePage();
	}
	
	public boolean isRegex(String str) {
		boolean result = false;
		Pattern p = Pattern.compile("^\\/");
		Matcher m = p.matcher(str);
		
		p = Pattern.compile("\\/$|\\/\\w+$");
		Matcher m2 = p.matcher(str);
		
		if (m.find() && m2.find()) {
			result = true;
		} else {
			result = false;
		}

		return result;
	}
	
	public String strToRegex(String val) {
		String result = "";
		val = val.replaceAll("\\\\", "\\\\\\\\");
		val = val.replaceAll("^/", "");
		val = val.replaceAll("/$", "");
		val = val.replaceAll("/\\w$", "");
		result = val;
		return result;
	}
	
	public boolean Assert(String value, String src) {
		boolean result = false;
		String msg = "";
		
		if (isRegex(value)) {
			value = this.strToRegex(value);
			Pattern p = Pattern.compile(value, Pattern.MULTILINE);
			Matcher m = p.matcher(src);
			if (m.find()) {
				this.PassedAsserts += 1;
				msg = String.format("Assert Passed, Found: '%s'.", value);
				this.Log(msg);
				result = true;
			} else {
				this.FailedAsserts += 1;
				msg = String.format("(!)Assert Failed for find: '%s'!", value);
				this._log(msg);
				this.SavePage();
				result = false;
			}
		} else {
			if (src.contains(value)) {
				this.PassedAsserts += 1;
				msg = String.format("Assert Passed, Found: '%s'.", value);
				this.Log(msg);
				result = true;				
			} else {
				this.FailedAsserts += 1;
				msg = String.format("(!)Assert Failed for find: '%s'!", value);
				this._log(msg);
				this.SavePage();
				result = false;
			}
		}
		
		return result;
	}
	
	public boolean AssertNot(String value, String src) {
		boolean result = false;
		String msg = "";
		
		if (isRegex(value)) {
			value = this.strToRegex(value);
			if (src.matches(value)) {
				this.FailedAsserts += 1;
				msg = String.format("(!)Assert Failed, Found Unexpected text: '%s'.", value);
				this._log(msg);
				this.SavePage();
				result = false;
			} else {
				this.PassedAsserts += 1;
				msg = String.format("Assert Failed for find: '%s' as expected.", value);
				this.Log(msg);
				result = true;
			}
		} else {
			if (src.contains(value)) {
				this.FailedAsserts += 1;
				msg = String.format("(!)Assert Passed, Found: '%s'.", value);
				this._log(msg);
				result = false;
			} else {
				this.PassedAsserts += 1;
				msg = String.format("Assert Failed for find: '%s' as expected.", value);
				this.Log(msg);
				this.SavePage();
				result = true;
			}
		}
		
		return result;
	}
	
	public void SavePage() {
		File dir = null;
		File newfd = null;
		String htmldir = this.resultDir;
		String new_save_file = "";
		String src = "";
		String md5 = "";
		
		if (!this.saveHTML) {
			return;
		}
		
		src = this.browser.getPageSource();
		md5 = SodaUtils.MD5(src);
		
		if (md5.compareTo(this.CurrentMD5) == 0) {
			this.Log(String.format("HTML Saved: %s", this.LastSavedPage));
			return;
		}
		
		htmldir = htmldir.concat("/saved-html");
		dir = new File(htmldir);
		if (!dir.exists()) {
			dir.mkdir();
		}
		
		new_save_file = htmldir;
		new_save_file = new_save_file.concat(String.format("/savedhtml-%d.html", this.SavePageNum));
		new_save_file = FilenameUtils.separatorsToSystem(new_save_file);
		this.SavePageNum += 1;
		
		newfd = new File(new_save_file);
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(newfd));
			bw.write(src);
			bw.close();
			this.LastSavedPage = new_save_file;
			this.CurrentMD5 = SodaUtils.MD5(src);
			this.Log(String.format("HTML Saved: %s", new_save_file));
		} catch (Exception exp) {
			this.ReportException(exp);
		}
	}
	
	protected void finalize() throws Throwable {
	    try {
	    	if (this.reportFD != null) {
	    		this.reportFD.close();
	    	}
	    } finally {
	        super.finalize();
	    }
	}
}
