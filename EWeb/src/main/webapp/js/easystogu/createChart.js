/**
 * Create the StockPrice and luzao chart
 * 
 * @returns {undefined}
 */
function createChart_LuZao(stockId, date_price, volume, data_ma19, data_ma43,
		data_ma86) {
	$('#container').highcharts('StockChart', {

		exporting : {
			enabled : false
		},
		rangeSelector : {
			selected : 1
		},

		title : {
			text : stockId
		},

		plotOptions : {
			candlestick : {
				color : '#00ff00',// Green
				upColor : '#ff0000'// Red
			}
		},

		yAxis : [ {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Price'
			},
			height : '80%',
			lineWidth : 2
		}, {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Volume'
			},
			top : '70%',
			height : '30%',
			offset : 0,
			lineWidth : 2
		} ],

		series : [ {
			type : 'candlestick',
			name : 'OHLC',
			data : date_price
		}, {
			name : 'MA19',
			data : data_ma19
		}, {
			name : 'MA43',
			data : data_ma43
		}, {
			name : 'MA86',
			data : data_ma86
		}, {
			type : 'column',
			name : 'Volume',
			data : volume,
			yAxis : 1
		} ]
	});

	chart = $('#container').highcharts();
}

/**
 * Create the StockPrice and luzao trend phase chart, and luzao statistics (4
 * phase) chart
 * 
 * @returns {undefined}
 */
function createChart_LuZao_Trend_Statistics(stockId, date_price, volume,
		data_ma19, data_ma43, data_ma86, data_1_guancha, data_2_jiancang,
		data_3_chigu, data_4_jiancang) {
	$('#container').highcharts('StockChart', {
		exporting : {
			enabled : false
		},
		
		rangeSelector : {
			selected : 1
		},

		title : {
			text : stockId
		},

		plotOptions : {
			candlestick : {
				color : '#00ff00',// Green
				upColor : '#ff0000'// Red
			}
		},

		yAxis : [ {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Price'
			},
			height : '80%',
			lineWidth : 2
		}, {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Statistics'
			},
			top : '70%',
			height : '30%',
			offset : 0,
			lineWidth : 2
		} ],

		series : [ {
			type : 'candlestick',
			name : 'OHLC',
			data : date_price
		}, {
			name : 'MA19',
			data : data_ma19
		}, {
			name : 'MA43',
			data : data_ma43
		}, {
			name : 'MA86',
			data : data_ma86
		}, {
			name : '??????????????????',
			data : data_1_guancha,
			yAxis : 1
		}, {
			name : '??????????????????',
			data : data_2_jiancang,
			yAxis : 1
		}, {
			name : '??????????????????',
			data : data_3_chigu,
			yAxis : 1
		}, {
			name : '??????????????????',
			data : data_4_jiancang,
			yAxis : 1
		} ]
	});

	chart = $('#container').highcharts();
}

/**
 * Create the StockPrice and luzao gordon dead chart, and luzao statistics (3
 * gordon and 2 dead ) chart
 * 
 * @returns {undefined}
 */
function createChart_LuZao_Gordon_Statistics(stockId, date_price, volume,
		data_ma19, data_ma43, data_ma86, data_1_gordon0, data_2_gordon1,
		data_3_gordon2, data_4_dead1, data_5_dead2) {
	$('#container').highcharts('StockChart', {
		exporting : {
			enabled : false
		},
		
		rangeSelector : {
			selected : 1
		},

		title : {
			text : stockId
		},

		plotOptions : {
			candlestick : {
				color : '#00ff00',// Green
				upColor : '#ff0000'// Red
			}
		},

		yAxis : [ {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Price'
			},
			height : '80%',
			lineWidth : 2
		}, {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Statistics'
			},
			top : '70%',
			height : '30%',
			offset : 0,
			lineWidth : 2
		} ],

		series : [ {
			type : 'candlestick',
			name : 'OHLC',
			data : date_price
		}, {
			name : 'MA19',
			data : data_ma19
		}, {
			name : 'MA43',
			data : data_ma43
		}, {
			name : 'MA86',
			data : data_ma86
		}, {
			name : '??????????????????',
			data : data_2_gordon1,
			yAxis : 1
		}, {
			name : '??????????????????',
			data : data_3_gordon2,
			yAxis : 1
		}, {
			name : '??????????????????',
			data : data_4_dead1,
			yAxis : 1
		}, {
			name : '??????????????????',
			data : data_5_dead2,
			yAxis : 1
		}, {
			name : '??????????????????',
			data : data_1_gordon0,
			yAxis : 1
		} ]
	});

	chart = $('#container').highcharts();
}

/**
 * Create the StockPrice and shenxian chart
 * 
 * @returns {undefined}
 */
function createChart_ShenXian(stockId, date_price, volume, data_h1, data_h2,
		data_h3) {
	$('#container').highcharts('StockChart', {
		exporting : {
			enabled : false
		},
		
		rangeSelector : {
			selected : 1
		},

		title : {
			text : stockId
		},

		plotOptions : {
			candlestick : {
				color : '#00ff00',// Green
				upColor : '#ff0000'// Red
			}
		},

		yAxis : [ {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Price'
			},
			height : '80%',
			lineWidth : 2
		}, {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Volume'
			},
			top : '70%',
			height : '30%',
			offset : 0,
			lineWidth : 2
		} ],

		series : [ {
			type : 'candlestick',
			name : 'OHLC',
			data : date_price
		}, {
			name : 'H1',
			data : data_h1
		}, {
			name : 'H2',
			data : data_h2
		}, {
			name : 'H3',
			data : data_h3
		}, {
			type : 'column',
			name : 'Volume',
			data : volume,
			yAxis : 1
		} ]
	});

	chart = $('#container').highcharts();
}

/**
 * Create the StockPrice and shenxianSell chart date_price: stock price volume:
 * volume data_h1: h2 data_h2: h2 data_hc5: ?????? price data_hc6: ?????? price
 * buy_flags: buy point sell_flags: sell point duo_flags: ???????????? suo_flags:
 * ?????????????????????
 * 
 * @returns {undefined}
 */
function createChart_ShenXianSell(stockId, date_price, volume, data_h1,
		data_h2, data_hc5, data_hc6, buy_flags, sell_flags, duo_flags,
		suo_flags) {
	$('#container').highcharts('StockChart', {
		exporting : {
			enabled : false
		},
		
		rangeSelector : {
			selected : 1
		},

		title : {
			text : stockId
		},

		plotOptions : {
			candlestick : {
				color : '#00ff00',// Green
				upColor : '#ff0000'// Red
			}
		},

		yAxis : [ {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Price'
			},
			height : '80%',
			lineWidth : 2
		}, {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Volume'
			},
			top : '70%',
			height : '30%',
			offset : 0,
			lineWidth : 2
		} ],

		series : [ {
			type : 'candlestick',
			name : 'OHLC',
			data : date_price,
			id : 'candlestick'
		}, {
			name : 'H1',
			data : data_h1
		}, {
			name : 'H2',
			data : data_h2
		}, {
			name : 'HC5',
			data : data_hc5,
			id : 'HC5'
		}, {
			name : 'HC6',
			data : data_hc6,
			id : 'HC6'
		}, {
			type : 'flags',
			data : sell_flags,
			onSeries : 'HC5'
		}, {
			type : 'column',
			name : 'Volume',
			data : volume,
			id : 'Volume',
			yAxis : 1
		}, {
			type : 'flags',
			data : buy_flags,
			onSeries : 'HC6'
		}, {
			type : 'flags',
			data : duo_flags,
			onSeries : 'HC6'
		}, {
			type : 'flags',
			data : suo_flags
		} ]
	});

	chart = $('#container').highcharts();
}

/**
 * Create the StockPrice and shenxian chart, and statistics chart
 * 
 * @returns {undefined}
 */
function createChart_ShenXian_Statistics(stockId, date_price, volume, data_h1,
		data_h2, data_h3, data_gordon, data_dead) {
	$('#container').highcharts('StockChart', {
		exporting : {
			enabled : false
		},
		
		rangeSelector : {
			selected : 1
		},

		title : {
			text : stockId
		},

		plotOptions : {
			candlestick : {
				color : '#00ff00',// Green
				upColor : '#ff0000'// Red
			}
		},

		yAxis : [ {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Price'
			},
			height : '80%',
			lineWidth : 2
		}, {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Statistics'
			},
			top : '70%',
			height : '30%',
			offset : 0,
			lineWidth : 2
		} ],

		series : [ {
			type : 'candlestick',
			name : 'OHLC',
			data : date_price
		}, {
			name : 'H1',
			data : data_h1
		}, {
			name : 'H2',
			data : data_h2
		}, {
			name : 'H3',
			data : data_h3
		}, {
			name : '??????????????????',
			data : data_gordon,
			yAxis : 1
		}, {
			name : '??????????????????',
			data : data_dead,
			yAxis : 1
		} ]
	});

	chart = $('#container').highcharts();
}

/**
 * Create the StockPrice and boll chart
 * 
 * @returns {undefined}
 */
function createChart_Boll(stockId, date_price, volume, data_mb, data_up,
		data_dn) {
	$('#container').highcharts('StockChart', {
		exporting : {
			enabled : false
		},
		
		rangeSelector : {
			selected : 1
		},

		title : {
			text : stockId
		},

		plotOptions : {
			candlestick : {
				color : '#00ff00',// Green
				upColor : '#ff0000'// Red
			}
		},

		yAxis : [ {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Price'
			},
			height : '80%',
			lineWidth : 2
		}, {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Volume'
			},
			top : '70%',
			height : '30%',
			offset : 0,
			lineWidth : 2
		} ],

		series : [ {
			type : 'candlestick',
			name : 'OHLC',
			data : date_price
		}, {
			name : 'MB',
			data : data_mb
		}, {
			name : 'UP',
			data : data_up
		}, {
			name : 'DN',
			data : data_dn
		}, {
			type : 'column',
			name : 'Volume',
			data : volume,
			yAxis : 1
		} ]
	});

	chart = $('#container').highcharts();
}

/**
 * Create the StockPrice and macd chart
 * 
 * @returns {undefined}
 */
function createChart_Macd(stockId, date_price, volume, data_dif, data_dea,
		data_macd) {
	$('#container').highcharts('StockChart', {
		exporting : {
			enabled : false
		},
		
		rangeSelector : {
			selected : 1
		},

		title : {
			text : stockId
		},

		plotOptions : {
			candlestick : {
				color : '#00ff00',// Green
				upColor : '#ff0000'// Red
			}
		},

		yAxis : [ {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Price'
			},
			height : '80%',
			lineWidth : 2
		}, {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'MACD'
			},
			top : '70%',
			height : '30%',
			offset : 0,
			lineWidth : 2
		} ],

		series : [ {
			type : 'candlestick',
			name : 'OHLC',
			data : date_price
		}, {
			name : 'DIF',
			data : data_dif,
			yAxis : 1
		}, {
			name : 'DEA',
			data : data_dea,
			yAxis : 1
		}, {
			name : 'MACD',
			data : data_macd,
			yAxis : 1
		} ]
	});

	chart = $('#container').highcharts();
}

/**
 * Create the StockPrice and macd statistics chart
 * 
 * @returns {undefined}
 */
function createChart_Macd_Statistics(stockId, date_price, volume, data_dif,
		data_dea, data_macd, data_gordon, data_dead) {
	$('#container').highcharts('StockChart', {
		exporting : {
			enabled : false
		},
		
		rangeSelector : {
			selected : 1
		},

		title : {
			text : stockId
		},

		plotOptions : {
			candlestick : {
				color : '#00ff00',// Green
				upColor : '#ff0000'// Red
			}
		},

		yAxis : [ {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Price'
			},
			height : '80%',
			lineWidth : 2
		}, {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Statistics'
			},
			top : '70%',
			height : '30%',
			offset : 0,
			lineWidth : 2
		} ],

		series : [ {
			type : 'candlestick',
			name : 'OHLC',
			data : date_price
		}, {
			name : '??????????????????',
			data : data_gordon,
			yAxis : 1
		}, {
			name : '??????????????????',
			data : data_dead,
			yAxis : 1
		} ]
	});

	chart = $('#container').highcharts();
}

/**
 * Create the StockPrice and magic9Day statistics chart
 * 
 * @returns {undefined}
 */
function createChart_Magic9Day_Statistics(stockId, date_price, volume, data_dif,
		data_dea, data_macd, data_up, data_down) {
	$('#container').highcharts('StockChart', {
		exporting : {
			enabled : false
		},
		
		rangeSelector : {
			selected : 1
		},

		title : {
			text : stockId
		},

		plotOptions : {
			candlestick : {
				color : '#00ff00',// Green
				upColor : '#ff0000'// Red
			}
		},

		yAxis : [ {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Price'
			},
			height : '80%',
			lineWidth : 2
		}, {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Statistics'
			},
			top : '70%',
			height : '30%',
			offset : 0,
			lineWidth : 2
		} ],

		series : [ {
			type : 'candlestick',
			name : 'OHLC',
			data : date_price
		}, {
			name : '??????9?????????????????????',
			data : data_up,
			yAxis : 1
		}, {
			name : '??????9?????????????????????',
			data : data_down,
			yAxis : 1
		} ]
	});

	chart = $('#container').highcharts();
}

/**
 * Create the StockPrice and qsdd chart
 * 
 * @returns {undefined}
 */
function createChart_Qsdd(stockId, date_price, volume, data_lonTerm,
		data_midTerm, data_shoTerm) {
	$('#container').highcharts('StockChart', {
		exporting : {
			enabled : false
		},
		
		rangeSelector : {
			selected : 1
		},

		title : {
			text : stockId
		},

		plotOptions : {
			candlestick : {
				color : '#00ff00',// Green
				upColor : '#ff0000'// Red
			}
		},

		yAxis : [ {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Price'
			},
			height : '80%',
			lineWidth : 2
		}, {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'QSDD'
			},
			top : '70%',
			height : '30%',
			offset : 0,
			lineWidth : 2
		} ],

		series : [ {
			type : 'candlestick',
			name : 'OHLC',
			data : date_price
		}, {
			name : 'LonTerm',
			data : data_lonTerm,
			yAxis : 1
		}, {
			name : 'MidTerm',
			data : data_midTerm,
			yAxis : 1
		}, {
			name : 'ShoTerm',
			data : data_shoTerm,
			yAxis : 1
		} ]
	});

	chart = $('#container').highcharts();
}

/**
 * Create the StockPrice and qsdd chart, and qsdd statistics (Top and Bottom)
 * chart.
 * 
 * @returns {undefined}
 */
function createChart_Qsdd_Statistics(stockId, date_price, volume, data_lonTerm,
		data_midTerm, data_shoTerm, date_topArea, date_bottomArea,
		date_bottomGordon) {
	$('#container').highcharts('StockChart', {
		exporting : {
			enabled : false
		},
		
		rangeSelector : {
			selected : 1
		},

		title : {
			text : stockId
		},

		plotOptions : {
			candlestick : {
				color : '#00ff00',// Green
				upColor : '#ff0000'// Red
			}
		},

		yAxis : [ {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Price'
			},
			height : '80%',
			lineWidth : 2
		}, {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'QSDD'
			},
			top : '70%',
			height : '30%',
			offset : 0,
			lineWidth : 2
		}, {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'QSDD'
			},
			top : '70%',
			height : '30%',
			offset : 0,
			lineWidth : 2
		} ],

		series : [ {
			type : 'candlestick',
			name : 'OHLC',
			data : date_price
		}, {
			name : '?????????',
			data : data_shoTerm,
			yAxis : 1
		}, {
			name : '?????????',
			data : data_midTerm,
			yAxis : 1
		}, {
			name : '?????????',
			data : data_lonTerm,
			yAxis : 1
		}, {
			name : '??????????????????',
			data : date_topArea,
			yAxis : 2
		}, {
			name : '??????????????????',
			data : date_bottomArea,
			yAxis : 2
		}, {
			name : '??????????????????',
			data : date_bottomGordon,
			yAxis : 2
		} ]
	});

	chart = $('#container').highcharts();
}

/**
 * Create the StockPrice Candlestick chart
 * 
 * @returns {undefined}
 */
function createChart_Candlestick(stockId, date_price, volume) {
	$('#container').highcharts('StockChart', {

		exporting : {
			enabled : false
		},
		
		rangeSelector : {
			selected : 1
		},

		title : {
			text : stockId
		},

		plotOptions : {
			candlestick : {
				color : '#00ff00',// Green
				upColor : '#ff0000'// Red
			}
		},

		yAxis : [ {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Price'
			},
			height : '80%',
			lineWidth : 2
		}, {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Volume'
			},
			top : '70%',
			height : '30%',
			offset : 0,
			lineWidth : 2
		} ],

		series : [ {
			type : 'candlestick',
			name : 'OHLC',
			data : date_price
		}, {
			type : 'column',
			name : 'Volume',
			data : volume,
			yAxis : 1
		} ]
	});

	chart = $('#container').highcharts();
}

/**
 * Create the StockPrice and wr chart, and qsdd statistics (Top and Bottom)
 * chart.
 * 
 * @returns {undefined}
 */
function createChart_WR_Statistics(stockId, date_price, volume, data_lonTerm,
		data_midTerm, data_shoTerm, date_topArea, date_bottomArea,
		date_bottomGordon) {
	$('#container').highcharts('StockChart', {

		exporting : {
			enabled : false
		},
		
		rangeSelector : {
			selected : 1
		},

		title : {
			text : stockId
		},

		plotOptions : {
			candlestick : {
				color : '#00ff00',// Green
				upColor : '#ff0000'// Red
			}
		},

		yAxis : [ {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Price'
			},
			height : '80%',
			lineWidth : 2
		}, {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Statistics'
			},
			top : '70%',
			height : '30%',
			offset : 0,
			lineWidth : 2
		}, {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Statistics'
			},
			top : '70%',
			height : '30%',
			offset : 0,
			lineWidth : 2
		} ],

		series : [ {
			type : 'candlestick',
			name : 'OHLC',
			data : date_price
		}, {
			name : '?????????',
			data : data_shoTerm,
			yAxis : 1
		}, {
			name : '?????????',
			data : data_midTerm,
			yAxis : 1
		}, {
			name : '?????????',
			data : data_lonTerm,
			yAxis : 1
		}, {
			name : '??????????????????',
			data : date_topArea,
			yAxis : 2
		}, {
			name : '??????????????????',
			data : date_bottomArea,
			yAxis : 2
		}, {
			name : '??????????????????',
			data : date_bottomGordon,
			yAxis : 2
		} ]
	});

	chart = $('#container').highcharts();
}

/**
 * Create the StockPrice and qsdd chart
 * 
 * @returns {undefined}
 */
function createChart_WR(stockId, date_price, volume, data_lonTerm,
		data_midTerm, data_shoTerm) {
	$('#container').highcharts('StockChart', {
		exporting : {
			enabled : false
		},
		
		rangeSelector : {
			selected : 1
		},

		title : {
			text : stockId
		},

		plotOptions : {
			candlestick : {
				color : '#00ff00',// Green
				upColor : '#ff0000'// Red
			}
		},

		yAxis : [ {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Price'
			},
			height : '80%',
			lineWidth : 2
		}, {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'WR'
			},
			top : '70%',
			height : '30%',
			offset : 0,
			lineWidth : 2
		} ],

		series : [ {
			type : 'candlestick',
			name : 'OHLC',
			data : date_price
		}, {
			name : 'LonTerm',
			data : data_lonTerm,
			yAxis : 1
		}, {
			name : 'MidTerm',
			data : data_midTerm,
			yAxis : 1
		}, {
			name : 'ShoTerm',
			data : data_shoTerm,
			yAxis : 1
		} ]
	});

	chart = $('#container').highcharts();
}

/**
 * Create the SameDigitsInHighPrice Stock statistics chart
 * 
 * @returns {undefined}
 */
function createChart_SameDigitsInHighPrice_Statistics(stockId, date_price,
		volume, data_count1, data_count2) {
	$('#container').highcharts('StockChart', {
		exporting : {
			enabled : false
		},
		
		rangeSelector : {
			selected : 1
		},

		title : {
			text : stockId
		},

		plotOptions : {
			candlestick : {
				color : '#00ff00',// Green
				upColor : '#ff0000'// Red
			}
		},

		yAxis : [ {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Price'
			},
			height : '80%',
			lineWidth : 2
		}, {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Statistics'
			},
			top : '70%',
			height : '30%',
			offset : 0,
			lineWidth : 2
		} ],

		series : [ {
			type : 'candlestick',
			name : 'OHLC',
			data : date_price
		}, {
			name : '????????????',
			data : data_count1,
			yAxis : 1
		}, {
			name : '????????????',
			data : data_count2,
			yAxis : 1
		} ]
	});

	chart = $('#container').highcharts();
}

/**
 * Create the XXXYuan Stock statistics chart
 * 
 * @returns {undefined}
 */
function createChart_XXXYuan_Statistics(stockId, date_price, volume, data_count) {
	$('#container').highcharts('StockChart', {
		exporting : {
			enabled : false
		},
		
		rangeSelector : {
			selected : 1
		},

		title : {
			text : stockId
		},

		plotOptions : {
			candlestick : {
				color : '#00ff00',// Green
				upColor : '#ff0000'// Red
			}
		},

		yAxis : [ {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Price'
			},
			height : '80%',
			lineWidth : 2
		}, {
			labels : {
				align : 'right',
				x : -3
			},
			title : {
				text : 'Statistics'
			},
			top : '70%',
			height : '30%',
			offset : 0,
			lineWidth : 2
		} ],

		series : [ {
			type : 'candlestick',
			name : 'OHLC',
			data : date_price
		}, {
			name : '????????????',
			data : data_count,
			yAxis : 1
		} ]
	});

	chart = $('#container').highcharts();
}
