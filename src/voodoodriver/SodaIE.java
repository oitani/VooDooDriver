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

import org.openqa.selenium.ie.InternetExplorerDriver;

public class SodaIE extends SodaBrowser implements SodaBrowserInterface {
	
	public SodaIE() {
		
	}
	
	public void setDownloadDirectory(String dir) {
		
	}
	
	public void newBrowser() {
		this.setDriver(new InternetExplorerDriver());
		this.setBrowserState(false);
	}
	
	public void forceClose() {
		SodaOSInfo.killProcesses(SodaOSInfo.getProcessIDs("iexplorer"));
		this.setBrowserClosed();
	}
	
	public void alertHack(boolean alert) {
		String alert_js =  "var old_alert = window.alert;\n" +
			"var old_confirm = window.confirm;\n"+
			"window.alert = function(){return " + alert + ";};\n"+
			"window.confirm = function(){return " + alert + ";};\n"+
			"window.onbeforeunload = null;\n"+
			"var result = 0;\nresult;\n";
		
		this.executeJS(alert_js, null);
	}
	
	
}
