package burp;

import com.codemagi.burp.PassiveScan;
import com.codemagi.burp.RuleTableComponent;
import com.codemagi.burp.ScanIssue;
import com.codemagi.burp.ScanIssueConfidence;
import com.codemagi.burp.ScanIssueSeverity;
import com.codemagi.burp.ScannerMatch;
import com.monikamorrow.burp.BurpSuiteTab;
import java.util.List;

/**
 * Burp Extender to find instances of applications revealing detailed error messages 
 * 
 * Some examples: 
 * <li>Fatal error: Call to a member function getId() on a non-object in /var/www/docroot/application/modules/controllers/ModalController.php on line 609
 * <li>[SEVERE] at net.minecraft.server.World.tickEntities(World.java:1146)
 * <li>Use of uninitialized value in string eq at /Library/Perl/5.8.6/WWW/Mechanize.pm line 695
 * 
 * @author August Detlefsen <augustd at codemagi dot com>
 * @contributor James Kettle (Ruby detection pattern)
 */
public class BurpExtender extends PassiveScan {

    public static final String ISSUE_NAME = "Detailed Error Messages Revealed";
    
    protected RuleTableComponent rulesTable;
    protected BurpSuiteTab mTab;
    
    @Override
    protected void initPassiveScan() {
	//set the extension Name
	extensionName = "Error Message Checks";
        
        //set the settings namespace
        settingsNamespace = "EMC_";
	
	rulesTable = new RuleTableComponent(this, callbacks, "https://raw.githubusercontent.com/augustd/burp-suite-error-message-checks/master/src/burp/match-rules.tab");
        
        mTab = new BurpSuiteTab(extensionName, callbacks);
        mTab.addComponent(rulesTable);
    }

    protected String getIssueName() {
	return ISSUE_NAME;
    }

    protected String getIssueDetail(List<com.codemagi.burp.ScannerMatch> matches) {
	com.codemagi.burp.ScannerMatch firstMatch = matches.get(0);

	StringBuilder description = new StringBuilder(matches.size() * 256);
	description.append("The application displays detailed error messages when unhandled ").append(firstMatch.getType()).append(" exceptions occur.<br>");
	description.append("Detailed technical error messages can allow an adversary to gain information about the application and database that could be used to conduct further attacks.");

	return description.toString();
    }
    
    protected ScanIssueSeverity getIssueSeverity(List<com.codemagi.burp.ScannerMatch> matches) {
	ScanIssueSeverity output = ScanIssueSeverity.INFO;
	for (ScannerMatch match : matches) {
	    //if the severity value of the match is higher, then update the stdout value
	    ScanIssueSeverity matchSeverity = match.getSeverity();
	    if (matchSeverity != null && 
		output.getValue() < matchSeverity.getValue()) {
		
		output = matchSeverity;
	    }
	}
	return output;
    }

    protected ScanIssueConfidence getIssueConfidence(List<com.codemagi.burp.ScannerMatch> matches) {
	ScanIssueConfidence output = ScanIssueConfidence.TENTATIVE;
	for (ScannerMatch match : matches) {
	    //if the severity value of the match is higher, then update the stdout value
	    ScanIssueConfidence matchConfidence = match.getConfidence();
	    if (matchConfidence != null && 
		output.getValue() < matchConfidence.getValue()) {
		
		output = matchConfidence;
	    }
	}
	return output;
    }
    
    @Override
    protected ScanIssue getScanIssue(IHttpRequestResponse baseRequestResponse, List<ScannerMatch> matches, List<int[]> startStop) {
        ScanIssueSeverity overallSeverity = getIssueSeverity(matches);
        ScanIssueConfidence overallConfidence = getIssueConfidence(matches);
        
	return new ScanIssue(
		baseRequestResponse, 
		helpers,
		callbacks, 
		startStop, 
		getIssueName(), 
		getIssueDetail(matches), 
		overallSeverity.getName(), 
		overallConfidence.getName());
    }
    
}