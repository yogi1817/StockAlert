package com.alexa.skill.stock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alexa.skill.stock.api.alpha.vantage.pojo.Value;
import com.alexa.skill.stock.util.LoadProperties;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;

public class StockAlertSpeechlet implements Speechlet{

	private static final Logger log = LoggerFactory.getLogger(StockAlertSpeechlet.class);
	
	private static final String STOCK_SLOT_1 = "StockNameOne";
	
	private static String url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=STOCK_NAME&apikey=W7WQEJ0I6WQ1MMJ2";
	
	@Override
	public void onSessionStarted(SessionStartedRequest request, Session session) throws SpeechletException {
		log.debug("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
	}

	@Override
	public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
		log.debug("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        return getWelcomeResponse();
	}

	@Override
	public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {
		
		log.debug("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;
        
        log.debug("intent "+intent+"|"+"intentName "+intentName);
        
        if ("HowIsMyStockDoing".equals(intentName)) {
            // Get the slots from the intent.
            Map<String, Slot> slots = intent.getSlots();

            // Get the stock slot from the list of slots.
            Slot stockSlot1 = slots.get(STOCK_SLOT_1);
            String stock1 = stockSlot1.getValue();
            
            log.debug("stock1 name is "+stock1);
            
            return getStockValueFromService(stock1);
        } else if ("AMAZON.HelpIntent".equals(intentName)) {
            // Create the plain text output.
            String speechOutput =
                    "With Skock Alert skill, you can get the latest update"
                    + " on you stock just by saying how is apple stock doing or just say the stock name";

            String repromptText = "Which stock do you want?";

            return getSpeechletResponse(speechOutput, repromptText, true);
        } else if ("AMAZON.StopIntent".equals(intentName)) {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText("Goodbye");

            return SpeechletResponse.newTellResponse(outputSpeech);
        } else if ("AMAZON.CancelIntent".equals(intentName)) {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText("Goodbye");

            return SpeechletResponse.newTellResponse(outputSpeech);
        } else {
            String errorSpeech = "This is unsupported.  Please try something else.";
            return getSpeechletResponse(errorSpeech, errorSpeech, true);
        }
	}

	@Override
	public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {
		 log.debug("inside onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
	                session.getSessionId());	
	}
	
	/**
     * Returns a Speechlet response for a speech and reprompt text.
     */
    private SpeechletResponse getSpeechletResponse(String speechText, String repromptText,
            boolean isAskResponse) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Session");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        log.debug(speechText);
        if (isAskResponse) {
            // Create reprompt
            PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
            repromptSpeech.setText(repromptText);
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(repromptSpeech);

            return SpeechletResponse.newAskResponse(speech, reprompt, card);

        } else {
            return SpeechletResponse.newTellResponse(speech, card);
        }
    }
    
	/**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getWelcomeResponse() {
    	log.debug("inside getWelcomeResponse");
        String speechText = "Welcome to the Alexa Skills Kit, "
        		+ "You can say how is google stock doing or just google";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Welcome");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    /**
     * This method gets the value of stock from the api
     * @param stockName
     * @return
     */
    public SpeechletResponse getStockValueFromService(String stockName) {
    	final StringJoiner speechText = new StringJoiner(" ");
        boolean isAskResponse = false;
        
        String stockCode = null;
        
        List<String> stockCodeList = getStockCodeList(stockName);
        
        if(stockCodeList.size()==0) {
        	speechText
        		.add("I'm not sure what your stock is? You can say something like, how is apple stock doing or just google")
        		.add(" and ");
            isAskResponse = true;
        }else if(stockCodeList.size()>1) {
        	speechText.add("I found more that one stock with this name, which one you want?");
        	stockCodeList.forEach(stockNameFromList -> 
        		speechText
        				.add(stockNameFromList.split(Pattern.quote("|"))[0].replaceAll("_"," "))
        				.add(" and "));
        	
            isAskResponse = true;
        }else { 
        	stockCode = stockCodeList.get(0).split(Pattern.quote("|"))[1];
	        Value value = null;

        	try {
				value = getStockValue(stockCode, stockName);
				log.debug("Value "+value);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
            speechText
            	.add(getSpeechText(value, stockName))
            	.add(" and ");
	    } 

        return getSpeechletResponse(speechText.toString().substring(0, speechText.length()-4), 
        			speechText.toString().substring(0, speechText.length()-4), isAskResponse);
    }
    
    /**
     * Filters on 2 stockName
     * @param name1
     * @param name2
     * @return
     */
    private List<String> getStockCodeList(String name1/*, String name2*/){
    	LoadProperties loadProperties = new LoadProperties();
    	
        List<String> stockCodeList = loadProperties.getCode(name1);
        log.debug("stockCodeList size"+ stockCodeList.size());
       
        return stockCodeList;
    }
    
    /**
     * This method generates the speech text
     * @param value
     * @param stock
     * @return
     */
    public String getSpeechText(Value value, String stock){
    	String gainLossEqual = null;
    	double percentage = 0;
    	if(value!=null){
    		if(value.getYesterdaysClose()<value.getTodaysClose()){
    			gainLossEqual = new String("gained");
    			percentage = ((value.getTodaysClose()-value.getYesterdaysClose())/value.getYesterdaysClose())*100;
    		}
    		else if(value.getYesterdaysClose()>value.getTodaysClose()){
    			gainLossEqual = new String("losses");
    			percentage = ((value.getYesterdaysClose()-value.getTodaysClose())/value.getTodaysClose())*100;
    		}
    		else{
    			gainLossEqual = new String("remians Equal");
    		}
    	}
    	return String.format("Your stock is %s. And today it %s from last day by %4.2f percentage "
    			+ "and the amount %s is %4.2f dollar, its current value is %4.2f", 
    			stock, gainLossEqual, percentage, gainLossEqual, 
    			Math.abs(value.getTodaysClose()-value.getYesterdaysClose()), value.getTodaysClose());
    }
    
    /**
     * This method call the api and get the value
     * @param stockName
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    public Value getStockValue(String stockCode, String stockName) throws MalformedURLException, IOException{
    	InputStream is = new URL(url.replace("STOCK_NAME", stockCode)).openStream();
        try {
          BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
          String jsonText = readAll(rd);
          
          JSONObject jObject  = new JSONObject(jsonText);
          JSONObject timeSeriesDaily  = jObject.getJSONObject("Time Series (Daily)");
          return getLatestValue(timeSeriesDaily, stockCode, stockName);

        } catch(Exception e){
        	log.error(e.getLocalizedMessage());
        }
        finally {
          is.close();
        }
		return null;
    }
    
    /**
     * 
     * @param timeSeriesDaily
     * @return
     */
    private Value getLatestValue(JSONObject timeSeriesDaily, String stockCode, String stockName){
    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Iterator<String> keysItr = timeSeriesDaily.keys();
        Value value = new Value();
        Date date = null;
        String key = null;
        JSONObject valueJson = null;
        TreeMap<Date, Value> stockMap = new TreeMap<Date, Value>(Collections.reverseOrder());
        
        while(keysItr.hasNext()) {
            key = keysItr.next();
            try {
				date = formatter.parse(key);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            valueJson = timeSeriesDaily.getJSONObject(key);
            
            value = new Value();
            value.setClose(valueJson.getDouble("4. close"));
            value.setStockCode(stockCode);
            value.setStockName(stockName);
            
            stockMap.put(date, value);
        }
        
        Value returnValue = new Value();
        returnValue.setTodaysClose(stockMap.firstEntry().getValue().getClose());
        stockMap.remove(stockMap.firstEntry().getKey());
        returnValue.setYesterdaysClose(stockMap.firstEntry().getValue().getClose());
        
        return returnValue;
    }
    
    /**
     * 
     * @param rd
     * @return
     * @throws IOException
     */
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
          sb.append((char) cp);
        }
        return sb.toString();
      }
}
