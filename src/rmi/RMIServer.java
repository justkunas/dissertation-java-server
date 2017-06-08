package rmi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONObject;
import org.json.XML;

public class RMIServer extends UnicastRemoteObject implements IRMIInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5162907943748051795L;

	private static String indexLocation;

	static String clipboard = "";
	static int count = 0;
	HashMap<String, Float> scores = new HashMap<String, Float>();
	ArrayList<String> resultFiles = new ArrayList<String>();
	HashMap<Integer, ArrayList<String>> pages = new HashMap<Integer, ArrayList<String>>();
	int page = 0;
	Integer pageTracker = 0;

	public RMIServer() throws RemoteException {
		super(1100);
		// TODO Auto-generated constructor stub

	}

	@Override
	public String search(String query) {
		System.out.println(query);
		JSONObject jsn = new JSONObject(query);
		resultFiles.clear();
		pages.clear();
		pageTracker = 0;
		String initPages = "";
		try {
			indexLocation = "C:\\Users\\Justkunas\\Documents\\Projects\\Index\\Dissertation Index";
			// *

			// *
			StandardAnalyzer analyser = new StandardAnalyzer();
			IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexLocation)));

			try {
				for (String bookQuery : generateQueries(jsn.get("query").toString())) {

					IndexSearcher search = new IndexSearcher(reader);
					TopScoreDocCollector collector = TopScoreDocCollector.create(2781403);

					QueryParser parser;
					String subXML = bookQuery.split("/:/g")[0];
					String fullQuery = bookQuery;

					switch (subXML) {
					case "place":
						parser = new QueryParser("places", analyser);
						break;

					case "character":
						parser = new QueryParser("characters", analyser);
						break;

					case "seriesitem":
						parser = new QueryParser("series", analyser);
						break;

					case "firstwordsitem":
						parser = new QueryParser("firstwords", analyser);
						break;

					case "lastwordsitem":
						parser = new QueryParser("lastwords", analyser);
						break;

					case "epigraph":
						parser = new QueryParser("epigraph", analyser);
						break;

					case "quotation":
						parser = new QueryParser("quotations", analyser);
						break;

					case "browseNode":
						parser = new QueryParser("browseNodes", analyser);
						break;

					case "subject":
						parser = new QueryParser("subject", analyser);
						break;

					default:
						parser = new QueryParser("content", analyser);
						break;
					}

					parser.setAllowLeadingWildcard(true);
					try {
						Query searchQuery = parser.parse(fullQuery);
						// System.out.println(fullQuery);
						// pause.nextLine();
						// System.out.println("Searching");
						search.search(searchQuery, collector);
						// System.out.println("Searched");
						ScoreDoc[] results = collector.topDocs().scoreDocs;

						for (ScoreDoc scoreDoc : results) {
							Document document = search.doc(scoreDoc.doc);

							JSONObject filters = jsn.getJSONObject("filters");

							float price = 0;
							int numberOfPages = 0;

							if (!document.get("listprice").equals(""))
								price = Float.parseFloat(document.get("listprice").substring(0));
														
							int priceMin = filters.getJSONObject("listprice").getInt("min");
							int priceMax = filters.getJSONObject("listprice").getInt("max");
							boolean priceEnabled = filters.getJSONObject("listprice").getBoolean("enabled");

							if (!document.get("numberofpages").equals(""))
								numberOfPages = Integer.parseInt(document.get("numberofpages"));

							int pagesMin = filters.getJSONObject("numberofpages").getInt("min");
							int pagesMax = filters.getJSONObject("numberofpages").getInt("max");
							boolean pagesEnabled = filters.getJSONObject("numberofpages").getBoolean("enabled");

							ArrayList<String> toAdd = new ArrayList<String>();

							boolean matchesFilters = true;
							
							System.out.println(pagesMin + " <= " + numberOfPages + " <= " + pagesMax);

							if (priceEnabled) {
								matchesFilters &= (price <= priceMax);
								//System.out.println((price <= priceMax));
								matchesFilters &= (price >= priceMin);
								//System.out.println((price >= priceMin));
							}

							if (pagesEnabled) {
								matchesFilters &= (numberOfPages >= pagesMin);
								matchesFilters &= (numberOfPages <= pagesMax);
							}

							if (matchesFilters) {

								if (pages.get(pageTracker) != null) {
									for (String s : pages.get(pageTracker)) {
										toAdd.add(s);
										//System.out.println(s);
									}
								}

								toAdd.add(document.get("path"));

								pages.put(pageTracker, toAdd);
							}

							if (pages.get(pageTracker) != null && pages.get(pageTracker).size() == 8)
								pageTracker++;

							// System.out.println("pages.get(" + pageTracker +
							// ").size(): " + pages.get(pageTracker).size());

						}
					} catch (Exception err) {
						err.printStackTrace();
					}
				}
			} catch (Exception err) {
				err.printStackTrace();
			}
			// *
			initPages = loadPages(query);
			
		} catch (Exception err) {

		}
		
		scores.clear();
		count = 0;
		return initPages;
	}

	public String loadPages(String query) {
		JSONObject json = new JSONObject();
		System.out.println(pages.size());
		for (String s : pages.get(page)) {
			try {
				File f = new File(s);
				if (f.getAbsolutePath().endsWith(".xml")) {
					FileReader fr = new FileReader(f);
					BufferedReader br = new BufferedReader(fr);

					String file = "";
					String line = "";

					do {

						file += line;
						line = br.readLine();

					} while (line != null);

					br.close();

					json.append("results", XML.toJSONObject(file));
				}
			} catch (IOException err) {
				err.printStackTrace();
			}
		}
		;
		return json.toString();
	}

	@Override
	public String nextPage() {
		if (pages.size() > (page + 1))
			page++;

		JSONObject json = new JSONObject();
		for (String s : pages.get(page)) {
			try {
				File f = new File(s);
				if (f.getAbsolutePath().endsWith(".xml")) {
					FileReader fr = new FileReader(f);
					BufferedReader br = new BufferedReader(fr);

					String file = "";
					String line = "";

					do {

						file += line;
						line = br.readLine();

					} while (line != null);

					br.close();

					json.append("results", XML.toJSONObject(file));
				}
			} catch (IOException err) {
				err.printStackTrace();
			}
		}
		;
		return json.toString();
	}

	@Override
	public String previousPage() {
		if (page - 1 >= 0)
			page--;

		JSONObject json = new JSONObject();
		for (String s : pages.get(page)) {
			try {
				File f = new File(s);
				if (f.getAbsolutePath().endsWith(".xml")) {
					FileReader fr = new FileReader(f);
					BufferedReader br = new BufferedReader(fr);

					String file = "";
					String line = "";

					do {

						file += line;
						line = br.readLine();

					} while (line != null);

					br.close();

					json.append("results", XML.toJSONObject(file));
				}
			} catch (IOException err) {
				err.printStackTrace();
			}
		}
		;
		return json.toString();
	}

	public HashMap<String, Float> sortResults(HashMap<String, Float> results) {
		return results.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	public String[] generateQueries(String query) {

		ArrayList<String> criteria = new ArrayList<String>();

		// *
		criteria.add("isbn:\"" + query + "\"^8");
		criteria.add("ean:\"" + query + "\"^8");
		criteria.add("dewey:\"" + query + "\"^8");

		criteria.add("title:\"" + query + "\"^7");

		criteria.add("subject:\"" + query + "\"^6");
		criteria.add("place:\"" + query + "\"^6");
		criteria.add("character:\"" + query + "\"^6");
		criteria.add("seriesitem:\"" + query + "\"^6");
		criteria.add("browseNode:\"" + query + "\"^6");

		criteria.add("edition:\"" + query + "\"^5");

		criteria.add("firstwordsitem:\"" + query + "\"^4");
		criteria.add("lastwordsitem:\"" + query + "\"^4");
		criteria.add("epigraph:\"" + query + "\"^4");

		criteria.add("manufacturer:\"" + query + "\"^3");
		criteria.add("publisher:\"" + query + "\"^3");

		// */
		criteria.add("label:\"" + query + "\"^2");
		// *
		criteria.add("readinglevel:\"" + query + "\"^2");
		criteria.add("studio:\"" + query + "\"^2");
		criteria.add("quotation:\"" + query + "\"^2");

		criteria.add("binding:\"" + query + "\"");
		// */

		return criteria.toArray(new String[0]);
	}
}
