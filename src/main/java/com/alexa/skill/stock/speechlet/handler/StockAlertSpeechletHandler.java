package com.alexa.skill.stock.speechlet.handler;

import java.util.HashSet;
import java.util.Set;

import com.alexa.skill.stock.StockAlertSpeechlet;
import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

public class StockAlertSpeechletHandler extends SpeechletRequestStreamHandler{

	private static final Set<String> supportedApplicationIds;
    static {
        /*
         * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
         * Alexa Skill and put the relevant Application Ids in this Set.
         */
        supportedApplicationIds = new HashSet<String>();
        supportedApplicationIds.add("amzn1.ask.skill.63afe5ce-f474-4f9a-b654-a0303f820307");
    }
   
	
	public StockAlertSpeechletHandler() {
		super(new StockAlertSpeechlet(), supportedApplicationIds);
	}
}
