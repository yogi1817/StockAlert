package com.alexa.skill.stock.api.alpha.vantage.pojo;


public class Value {

	private double todaysClose;
	private double yesterdaysClose;
	private String stockName;
	private String stockCode;
	private double close;
	
	/**
	 * @return the todaysClose
	 */
	public double getTodaysClose() {
		return todaysClose;
	}


	/**
	 * @param todaysClose the todaysClose to set
	 */
	public void setTodaysClose(double todaysClose) {
		this.todaysClose = todaysClose;
	}


	/**
	 * @return the yesterdaysClose
	 */
	public double getYesterdaysClose() {
		return yesterdaysClose;
	}


	/**
	 * @param yesterdaysClose the yesterdaysClose to set
	 */
	public void setYesterdaysClose(double yesterdaysClose) {
		this.yesterdaysClose = yesterdaysClose;
	}


	/**
	 * @return the stockName
	 */
	public String getStockName() {
		return stockName;
	}


	/**
	 * @param stockName the stockName to set
	 */
	public void setStockName(String stockName) {
		this.stockName = stockName;
	}


	/**
	 * @return the stockCode
	 */
	public String getStockCode() {
		return stockCode;
	}


	/**
	 * @param stockCode the stockCode to set
	 */
	public void setStockCode(String stockCode) {
		this.stockCode = stockCode;
	}


	/**
	 * @return the close
	 */
	public double getClose() {
		return close;
	}


	/**
	 * @param close the close to set
	 */
	public void setClose(double close) {
		this.close = close;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Value [todaysClose=" + todaysClose + ", yesterdaysClose="
				+ yesterdaysClose + ", stockName=" + stockName + ", stockCode="
				+ stockCode + ", close=" + close + "]";
	}
}
