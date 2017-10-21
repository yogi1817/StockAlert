package test.StockAlertSpeechlet;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Test;

import com.alexa.skill.stock.StockAlertSpeechlet;
import com.alexa.skill.stock.api.alpha.vantage.pojo.Value;

public class StockAlertSpeechletTest {

	StockAlertSpeechlet stockAlertSpeechlet = new StockAlertSpeechlet();
	
	@Test
	public void testStockValue() throws MalformedURLException, IOException {
		Value value = stockAlertSpeechlet.getStockValue("CTSH");
		System.out.println(value);
	}
	
	@Test
	public void testSpeechText() throws MalformedURLException, IOException {
		Value value = stockAlertSpeechlet.getStockValue("CTSH");
		System.out.println(stockAlertSpeechlet.getSpeechText(value, "CSTH"));
		System.out.println(value);
	}
}
