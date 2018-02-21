/*
 * ORDER OF KEYS:
 * 1) setOAuthConsumerKey
 * 2) setOAuthConsumerSecret
 * 3) setOAuthAccessToken
 * 4) setOAuthAccessTokenSecret
 * 
 * NOTE: When adding more keys increment ACTIVE_APPS
 * */
public class APIKeys {
	public static final int PARAMETERS = 4;
	public static int ACTIVE_APPS = 10;
	public static String configs[][];
	
	public static String[][] getConfigs() {
		if(configs != null)
			return configs;
		configs = new String[PARAMETERS][ACTIVE_APPS];
		//Archeopteryx
		configs[0][0] = "wvXUnPPXgrpDLyWadNGGPWzAM";
		configs[1][0] = "j18fdzJYEtU80PThySwP7g3w693b1o1gVRqLuxwBWc44OiYZrN";
		configs[2][0] = "2827656501-bEo9j6FwUfzKScImVgd1awk8frOpRLkx0UhLngq";
		configs[3][0] = "yurNr4EdLso6RzMdzIMDrOnSA9mWku6Ss8QIMRL7A9Fj0";
		//Archeopteryx1
		configs[0][1] = "GcDdCvb7ZGwcgnUwhDOxpp8Sg";
		configs[1][1] = "6jY7o2mBs81nowy0zjhvXW2GA5qpL0EfT8pmZagvc6vuy0XDTo";
		configs[2][1] = "2827656501-zsI90f4uztHXK7PvvZX3tlFKai84tdyszO5iElz";
		configs[3][1] = "AYDmw94Z7QetZpIjHVlArmdrDmkWn1QGakY5L0aFGbyY6";
		//Archeopteryx2
		configs[0][2] = "XSr7I1Z5yC3hoav4YokS0QdNT";
		configs[1][2] = "TJc7BLpR7BKpJH0QQ0YGv2NmyRYbaDdtiJWLiv829ZRvb3zJwD";
		configs[2][2] = "2827656501-4OllpPgwNPKwymZFbQ8qrPHSsyhowxOoNIWNGIu";
		configs[3][2] = "hJpsruEYtfbafU6xJAxS8JhESXu7fyw5EOveD1EVutZ06";
		//Archeopteryx3
		configs[0][3] = "oJExX0tPeTjF5tHq62lX2vLhk";
		configs[1][3] = "wjZiReaxlPfAxwhxbwJoYJpNyqhwdNN3Vqt2CZoSmgfrRHZ0cj";
		configs[2][3] = "2827656501-KwQJ0h9SbPrB920H5iGbN0esfTvmBbGc97P312S";
		configs[3][3] = "HLtWkrVAhkxAYxJEXanffq80ZGLBgQDUWEMKuS8PFIS10";
		//Archeopteryx4
		configs[0][4] = "YvIFdJ8h9eaKbz9JnDY7YJUYp";
		configs[1][4] = "sGVPOGKUkYiLVCPWKOXNgljcQWqf1iZJ1lhEOmZZgymOZpspC2";
		configs[2][4] = "2827656501-a3lId8jMaDZxWW42T9ShM3fXbLIHYxedvnhyFS3";
		configs[3][4] = "6qxtXQdMiLhn4JJu0cM10XEerH4p5XRUNQtIOyFUim4ak";
		//Archeopteryx5
		configs[0][5] = "6I6e6DCoeTqsHesfXNq8rjuE9";
		configs[1][5] = "cgTJiUq1ZFrBSb5rHdeDYwWQAS4aNvqNF28GBQJyBsluevsqLq";
		configs[2][5] = "2827656501-TNnH2IUoujihgO9b6gVsAg7KEisvgJlbeL2xFmo";
		configs[3][5] = "RGJZwuFO3RSRaM6kMVPIdZVEhIiHsS5dwEpii80nq6mou";
		//Archeopteryx6
		configs[0][6] = "h9DaBHQDQOIReOhRiPBLthTzl";
		configs[1][6] = "lSqNVbsnYATA6LkhZdG6BY0H5VwWgoloXLPXaZBnjDuXkZUqIV";
		configs[2][6] = "2827656501-ivmRLpJi1k2i1RVhZVkuza98J8Egc2k1PzFv2oo";
		configs[3][6] = "VnMAwAMW9NoGQYzMnGEA3PhLzPbUdaDIXG3lEICwJcHsB";
		//Archeopteryx7
		configs[0][7] = "HMtElftjoKufH6dM8U8I4E34g";
		configs[1][7] = "grdnq3hRbyivYcN4ocyKQ392DHGhxfaERqGWsnCR8qQZT5k2E7";
		configs[2][7] = "2827656501-bMIoXfGABRc1NCvwjnlb827qiZchhLNrZjfjitM";
		configs[3][7] = "eLszh3hyWSFLQjDa3KvYSOimdtdd3iPHgcIOfAe11n5PY";
		//Archeopteryx8
		configs[0][8] = "xQSI3SJyR2twwCSZM0MVKTGOf";
		configs[1][8] = "rL18kAXZmvrYfLZ196x7cOOwmPVEYd65vLWSHoDF3CUfT1CRVE";
		configs[2][8] = "2827656501-gOPynSkLMLjNu6D6bXgp3PMzH4OSA64PKs3Q1VW";
		configs[3][8] = "iQBe8Bix4cfcrl9KchumrnKYvK2RViN5vWMvjQjieirGm";
		//Archeopteryx9
		configs[0][9] = "1mxAovt4j4cCexSnIdvQm4YNt";
		configs[1][9] = "TdDIYoixqDtYD4let21cEkdGN71TwLNdOf8MxYlku8SGBFpuqo";
		configs[2][9] = "2827656501-R9dsitTvMzVh5LzmiGZOgc8fczPH6Fhe2Oszpau";
		configs[3][9] = "QKJPWTWqSdjjGxdf96V9iMoY4tDsfFZPmk18sORVfpGuO";
		return configs;
	}
}
