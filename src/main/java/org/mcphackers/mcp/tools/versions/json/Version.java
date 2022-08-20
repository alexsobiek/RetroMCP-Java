package org.mcphackers.mcp.tools.versions.json;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

/**
 * Deserialized version JSON
 */
public class Version {
	
	public AssetIndex assetIndex;
	public String assets;
	public VersionDownloads downloads;
	public String id;
	public String time;
	public String releaseTime;
	public String type;
	public List<DependDownload> libraries;
	//public Logging logging;
	public String mainClass;
	public String minecraftArguments;
	public Arguments arguments;
	
	public static Version from(JSONObject obj) {
		if(obj == null) {
			return null;
		}
		return new Version() {
			{
				assetIndex = AssetIndex.from(obj.getJSONObject("assetIndex"));
				assets = obj.getString("assets");
				downloads = VersionDownloads.from(obj.getJSONObject("downloads"));
				id = obj.getString("id");
				time = obj.getString("time");
				releaseTime = obj.getString("releaseTime");
				type = obj.getString("type");
				libraries = new ArrayList<>();
				for(Object o : obj.getJSONArray("libraries")) {
					if(o instanceof JSONObject) {
						libraries.add(DependDownload.from((JSONObject)o));
					}
				}
				mainClass = obj.getString("mainClass");
				minecraftArguments = obj.optString("minecraftArguments", null);
			}
		};
	}
	
	public static class VersionDownloads {

		public Download client;
		public Download server;
		public Download windows_server;
		public Download client_mappings;
		public Download server_mappings;
		
		public static VersionDownloads from(JSONObject obj) {
			if(obj == null) {
				return null;
			}
			return new VersionDownloads() {
				{
					client = Download.from(obj.optJSONObject("client"));
					server = Download.from(obj.optJSONObject("server"));
					windows_server = Download.from(obj.optJSONObject("windows_server"));
					client_mappings = Download.from(obj.optJSONObject("client_mappings"));
					server_mappings = Download.from(obj.optJSONObject("server_mappings"));
				}
			};
		}
	}
	
	public static class Arguments {
		public List<Object> game;
		public List<Object> jvm;
		
		public static Arguments from(JSONObject obj) {
			if(obj == null) {
				return null;
			}
			return new Arguments() {
				{
					for(Object o : obj.getJSONArray("game")) {
						if(o instanceof JSONObject) {
							game.add(Argument.from((JSONObject)o));
						}
						game.add(o);
					}
					for(Object o : obj.getJSONArray("jvm")) {
						if(o instanceof JSONObject) {
							jvm.add(Argument.from((JSONObject)o));
						}
						jvm.add(o);
					}
				}
			};
		}
	}
	
	public static class Argument {
		public List<Rule> rules;
		public Object value;
		
		public static Argument from(JSONObject obj) {
			if(obj == null) {
				return null;
			}
			return new Argument() {
				{
					for(Object o : obj.getJSONArray("rules")) {
						rules.add(Rule.from((JSONObject)o));
					}
					value = obj.get("value");
				}
			};
		}
	}

}
