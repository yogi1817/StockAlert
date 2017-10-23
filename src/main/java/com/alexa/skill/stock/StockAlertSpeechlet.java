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
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alexa.skill.stock.api.alpha.vantage.pojo.Value;
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
	
	private static final String STOCK_SLOT = "Stock";
	
	private static final String STOCK_KEY = "STOCKKEY";
	
	private static String url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=CTSH&apikey=W7WQEJ0I6WQ1MMJ2";
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
        if ("SetMyStock".equals(intentName)) {
            return setStockInSession(intent, session);
        } else if ("HowIsMyStockDoing".equals(intentName)) {
            return getStockValueFromService(intent, session);
        } else {
            throw new SpeechletException("Invalid Intent");
        }
	}

	@Override
	public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {
		 log.debug("inside onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
	                session.getSessionId());	
	}
	
	private SpeechletResponse setStockInSession(final Intent intent, final Session session) {
		// Get the slots from the intent.
        Map<String, Slot> slots = intent.getSlots();

        // Get the stock slot from the list of slots.
        Slot favoriteStockSlot = slots.get(STOCK_SLOT);
        String speechText, repromptText;

        // Check for favorite stock and create output to user.
        if (favoriteStockSlot != null) {
        	log.debug("favoriteStockSlot.getValue() "+ favoriteStockSlot.getValue());
            // Store the user's favorite stock in the Session and create response.
            String favoriteStock = favoriteStockSlot.getValue();
            session.setAttribute(STOCK_KEY, favoriteStock);
            speechText =
                    String.format("I now know that your favorite stock is %s. You can ask me your "
                            + "favorite stock by saying, how's my stock doing?", favoriteStock);
            repromptText =
                    "You can ask me your stock by saying, how's my stock doing?";

        } else {
        	log.debug("invalid stock name");
            // Render an error since we don't know what the users favorite stock is.
            speechText = "I'm not sure what your favorite stock is, please try again";
            repromptText =
                    "I'm not sure what your favorite stock is. You can tell me your favorite "
                            + "stock by saying, my favorite stock is ctsh pr acn";
        }

        return getSpeechletResponse(speechText, repromptText, true);
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
        String speechText = "Welcome to the Alexa Skills Kit, You can set your stock by saying My favourite Stock is CTSH or ACN";

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

    private SpeechletResponse getStockValueFromService(final Intent intent, final Session session) {
    	String speechText;
        boolean isAskResponse = false;

        // Get the user's favorite stock from the session.
        String favoriteStock = (String) session.getAttribute(STOCK_KEY);

        Value value = null;
        // Check to make sure user's favorite stock is set in the session.
        if (StringUtils.isNotEmpty(favoriteStock)) {
        	log.debug("favoriteStock from  getStockValueFromService"+ favoriteStock);
        	try {
				value = getStockValue(favoriteStock);
				log.debug("Value "+value);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
            speechText = getSpeechText(value, favoriteStock);
        } else {
            // Since the user's favorite stock is not set render an error message.
            speechText =
                    "I'm not sure what your favorite stock is. You can say, my favorite stock is ctsh or acn";
            isAskResponse = true;
        }

        return getSpeechletResponse(speechText, speechText, isAskResponse);
    }
    
    public String getSpeechText(Value value, String favoriteStock){
    	String gainLossEqual = null;
    	double percentage = 0;
    	if(value!=null){
    		if(value.getOpen()<value.getClose()){
    			gainLossEqual = new String("gained");
    			percentage = ((value.getClose()-value.getOpen())/value.getOpen())*100;
    		}
    		else if(value.getOpen()>value.getClose()){
    			gainLossEqual = new String("losses");
    			percentage = ((value.getOpen()-value.getClose())/value.getOpen())*100;
    		}
    		else{
    			gainLossEqual = new String("remians Equal");
    		}
    	}
    	return String.format("Your favorite stock is %s. And today it %s from last day by %4.2f percentage "
    			+ "and the amount %s is %4.2f", 
    			favoriteStock, gainLossEqual, percentage, gainLossEqual, Math.abs(value.getOpen()-value.getClose()));
    }
    
    public Value getStockValue(String stockName) throws MalformedURLException, IOException{
    	InputStream is = new URL(url.replace("STOCK_NAME", stockName)).openStream();
        try {
          BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
          String jsonText = readAll(rd);
          
          JSONObject jObject  = new JSONObject(jsonText);
          JSONObject timeSeriesDaily  = jObject.getJSONObject("Time Series (Daily)");
          return getLatestValue(timeSeriesDaily);

        } catch(Exception e){
        	log.error(e.getLocalizedMessage());
        }
        finally {
          is.close();
        }
		return null;
    }
    
    private Value getLatestValue(JSONObject timeSeriesDaily){
    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Iterator<String> keysItr = timeSeriesDaily.keys();
        Value value = new Value();
        Date date = null;
        String key = null;
        JSONObject valueJson = null;
        TreeMap<Date, Value> stockMapPerMin = new TreeMap<Date, Value>(Collections.reverseOrder());
        
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
            value.setOpen(valueJson.getDouble("1. open"));
            value.setHigh(valueJson.getDouble("2. high"));
            value.setLow(valueJson.getDouble("3. low"));
            value.setClose(valueJson.getDouble("4. close"));
            value.setVolume(valueJson.getDouble("5. volume"));
            
            stockMapPerMin.put(date, value);
        }
        return stockMapPerMin.firstEntry().getValue();
    }
    
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
          sb.append((char) cp);
        }
        return sb.toString();
      }
}
