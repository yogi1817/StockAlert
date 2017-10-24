package test.StockAlertSpeechlet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.alexa.skill.stock.StockAlertSpeechlet;
import com.alexa.skill.stock.api.alpha.vantage.pojo.Value;
import com.alexa.skill.stock.util.LoadProperties;
import com.amazon.speech.speechlet.SpeechletResponse;

public class StockAlertSpeechletTest {

	StockAlertSpeechlet stockAlertSpeechlet = new StockAlertSpeechlet();
	LoadProperties loadProp = new LoadProperties();
	
	@Test
	public void testStockValue() throws MalformedURLException, IOException {
		Value value = stockAlertSpeechlet.getStockValue("CTSH");
		Assert.assertNotNull(value);
	}
	
	@Test
	public void testSpeechText() throws MalformedURLException, IOException {
		Value value = stockAlertSpeechlet.getStockValue("CTSH");
		Assert.assertNotNull(value);
	}
	
	@Test
	public void loadPropFile() throws MalformedURLException, IOException {
		List<String> code = loadProp.getCode("apple");
		Assert.assertNotNull(code);
	}
	
	@Test
	public void loadPropFileForGoogle() throws MalformedURLException, IOException {
		List<String> code = loadProp.getCode("google");
		Assert.assertNotNull(code);
	}
	
	@Test
	public void loadStockValue() throws MalformedURLException, IOException {
		SpeechletResponse speechletResponse = stockAlertSpeechlet.getStockValueFromService("apple", null);
		Assert.assertTrue(speechletResponse!=null);
	}
	
	@Test
	public void loadStockValueGoogle() throws MalformedURLException, IOException {
		SpeechletResponse speechletResponse = stockAlertSpeechlet.getStockValueFromService("google", null);
		Assert.assertTrue(speechletResponse!=null);
	}
	
	@Test
	public void loadStockValueunknownStock() throws MalformedURLException, IOException {
		SpeechletResponse speechletResponse = stockAlertSpeechlet.getStockValueFromService("unknownStock", null);
		Assert.assertTrue(speechletResponse!=null);
	}
	
	@Test
	public void loadStockValueCiscoSystem() throws MalformedURLException, IOException {
		SpeechletResponse speechletResponse = stockAlertSpeechlet.getStockValueFromService("Cisco", "Systems");
		Assert.assertTrue(speechletResponse!=null);
	}
	
	@Test
	public void loadStockValueIsApple() throws MalformedURLException, IOException {
		SpeechletResponse speechletResponse = stockAlertSpeechlet.getStockValueFromService("is", "apple");
		Assert.assertTrue(speechletResponse!=null);
	}
}
