package es.virtuafit.gestplus.firebase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class PushNotificationHelper {
	public final static String AUTH_KEY_FCM = "AAAA0Vpoz0g:APA91bFLhKXRuigm-mbtlc-Z5SihyPja6ZKZlkEpOW0RGH2b9yvNNHBAMNxT9G8g6mAKcAi76ORJCxMPvfH_XWst7jSoi4-Eys9ddjimFWzbG6JA5dkBJTVYzZMEyJyKKFQzMxRhWGUU";
	public final static String API_URL_FCM = "https://fcm.googleapis.com/fcm/send";
	public final static String API_APP_CENTER_IOS = "https://api.appcenter.ms/v0.1/apps/Wiemspro/okmas/push/notifications";
	public final static String API_APP_CENTER_ANDORID = "https://api.appcenter.ms/v0.1/apps/Wiemspro/okeymas_android/push/notifications";
	public final static String AUTH_KEY_APP_CENTER = "9d8b33ab1a14d3c2eff6f8b22a168d7af826f868";
	public final static String API_ONESIGNAL = "https://api.onesignal.com/notifications";
	public final static String ONESIGNAL_APP_ID = "f95f0286-c769-417f-b1c9-4cf9553f7473";
	public final static String ONESIGNAL_REST_API_KEY = "os_v2_app_7fpqfbwhnfax7mojjt4vkp3uopj3hguka2xexhn5wvney3nyywcpegujgexl6qvk6p236y6veljdocg5shsg2fo2yln27rlrjlviioa";
	public final static String ONESIGNAL_APP_ID_WIEMSPRO = "6ea3c7c4-da49-4777-ae77-8721f5990bc0";
	public final static String ONESIGNAL_REST_API_KEY_WIEMSPRO = ""; // TODO: fill in REST API Key from OneSignal Wiemspro app dashboard
	public final static String ONESIGNAL_ICON = "iconoamarillo";

	
	public static String sendPushNotification(String deviceToken, String message) throws IOException {
		String result = "";
		URL url = new URL(API_URL_FCM);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);

		conn.setRequestMethod("POST");
		conn.setRequestProperty("Authorization", "key=" + AUTH_KEY_FCM);
		conn.setRequestProperty("Content-Type", "application/json");

		String jsonBody = "{"
				+ "\"to\": \"" + deviceToken.trim() + "\","
				+ "\"notification\": {"
				+ "\"title\": \"Pesaje!\","
				+ "\"body\": \"" + message + "\""
				+ "}"
				+ "}";
		try {
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(jsonBody);
			wr.flush();

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}
			result = "ok";
		} catch (Exception e) {
			e.printStackTrace();
			result = "ko";
		}
		System.out.println("GCM Notification is sent successfully");

		return result;
	}
	public static String sendPushNotificationTanita(String deviceToken, String platform,String email) throws IOException {
		String result = "";
		URL url;
		if (platform.equals("android")) {
			url=new URL(API_APP_CENTER_ANDORID);
		}else {
			url=new URL(API_APP_CENTER_IOS);
		}
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);

		conn.setRequestMethod("POST");
		conn.setRequestProperty("X-API-Token", AUTH_KEY_APP_CENTER);
		conn.setRequestProperty("Content-Type", "application/json");

		try {
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write("{\n" + 
					" \"notification_content\" : {\n" + 
					"  \"name\" : \"Se han procesado los datos de su pesaje\",\n" + 
					"  \"title\" : \"Pesaje\",\n" + 
					"  \"body\" : \"Pulse para ver los datos de su último pesaje\",\n" + 
					"  \"custom_data\" : {\"key1\" : \"Salud\",\"key2\" : \"" + email +"\"}\n" + 
					"  },\n" + 
					"    \"notification_target\" : {\n" + 
					"    \"type\" : \"devices_target\",\n" + 
					"    \"devices\" : [\"" + deviceToken + "\"]\n" + 
					"  }\n" + 
					"}");
			wr.flush();

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}
			result = "ok";
		} catch (Exception e) {
			e.printStackTrace();
			result = "ko";
		}
		System.out.println("GCM Notification is sent successfully");

		return result;
	}
	public static String sendPushNotificationGeneral(String keyApp,String miURL,String deviceToken,String mensaje,String titulo) throws IOException {
		String result = "";
		URL url;
		url=new URL("https://api.appcenter.ms/v0.1/apps/"+ miURL + "/push/notifications");
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);

		conn.setRequestMethod("POST");
		conn.setRequestProperty("X-API-Token", keyApp);
		conn.setRequestProperty("Content-Type", "application/json");

		try {
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			if (miURL.toLowerCase().contains("tsu")) {
				wr.write("{\n" + 
						" \"notification_content\" : {\n" + 
						"  \"name\" : \""+ titulo + "\",\n" + 
						"  \"title\" : \""+ titulo + "\",\n" + 
						"  \"body\" : \""+ mensaje + "\",\n" + 
						"  \"custom_data\" : {\"Titulo\" : \"" + titulo + "\",\"Cuerpo\" : \"" + mensaje +"\"}\n" + 
						"  },\n" + 
						"    \"notification_target\" : {\n" + 
						"    \"type\" : \"devices_target\",\n" + 
						"    \"devices\" : [\"" + deviceToken + "\"]\n" + 
						"  }\n" + 
						"}");
				wr.flush();
			}else {
				wr.write("{\n" + 
						" \"notification_content\" : {\n" + 
						"  \"name\" : \""+ titulo + "\",\n" + 
						"  \"title\" : \""+ titulo + "\",\n" + 
						"  \"body\" : \""+ mensaje + "\",\n" + 
						"  },\n" + 
						"    \"notification_target\" : {\n" + 
						"    \"type\" : \"devices_target\",\n" + 
						"    \"devices\" : [\"" + deviceToken + "\"]\n" + 
						"  }\n" + 
						"}");
				wr.flush();
			}
			

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}
			result = "ok";
		} catch (Exception e) {
			e.printStackTrace();
			result = "ko";
		}
		System.out.println("GCM Notification is sent successfully");

		return result;
	}
	public static String sendPushNotificationGeneralTodos(String keyApp,String miURL,String mensaje,String titulo) throws IOException {
		String result = "";
		URL url;
		url=new URL("https://api.appcenter.ms/v0.1/apps/"+ miURL + "/push/notifications");
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);

		conn.setRequestMethod("POST");
		conn.setRequestProperty("X-API-Token", keyApp);
		conn.setRequestProperty("Content-Type", "application/json");

		try {
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write("{\n" + 
						" \"notification_content\" : {\n" + 
						"  \"name\" : \""+ titulo + "\",\n" + 
						"  \"title\" : \""+ titulo + "\",\n" + 
						"  \"body\" : \""+ mensaje + "\",\n" + 
						"  }\n" + 
						"}");
				wr.flush();
			

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}
			result = "ok";
		} catch (Exception e) {
			e.printStackTrace();
			result = "ko";
		}
		System.out.println("GCM Notification is sent successfully");

		return result;
	}
	public static String sendPushNotificationGeneralAdmin(String keyApp,String miURL,String deviceToken,String clave,String valor) throws IOException {
		String result = "";
		URL url;
		url=new URL("https://api.appcenter.ms/v0.1/apps/"+ miURL + "/push/notifications");
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);

		conn.setRequestMethod("POST");
		conn.setRequestProperty("X-API-Token", keyApp);
		conn.setRequestProperty("Content-Type", "application/json");

		try {
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write("{\n" + 
						" \"notification_content\" : {\n" + 
						"  \"name\" : \""+ clave + "\",\n" + 
						"  \"title\" : \""+ clave + "\",\n" + 
						"  \"body\" : \""+ clave + "\",\n" + 
						"  \"custom_data\" : {\"" + clave+"\" : \"" + valor +"\"}\n" + 
						"  },\n" + 
						"    \"notification_target\" : {\n" + 
						"    \"type\" : \"devices_target\",\n" + 
						"    \"devices\" : [\"" + deviceToken + "\"]\n" + 
						"  }\n" + 
						"}");
				wr.flush();
			

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}
			result = "ok";
		} catch (Exception e) {
			e.printStackTrace();
			result = "ko";
		}
		System.out.println("GCM Notification is sent successfully");

		return result;
	}
	public static String sendPushNotificationRegisterAppCenter(String deviceToken, String platform,String email) throws IOException {
		String result = "";
		URL url;
		if (platform.equals("android")) {
			url=new URL(API_APP_CENTER_ANDORID);
		}else {
			url=new URL(API_APP_CENTER_IOS);
		}
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);

		conn.setRequestMethod("POST");
		conn.setRequestProperty("X-API-Token", AUTH_KEY_APP_CENTER);
		conn.setRequestProperty("Content-Type", "application/json");

		try {
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write("{\n" + 
					" \"notification_content\" : {\n" + 
					"  \"name\" : \"Pulse para indicar su contraseña\",\n" + 
					"  \"title\" : \"Contraseña\",\n" + 
					"  \"body\" : \"Pulse para indicar su contraseña\",\n" + 
					"  \"custom_data\" : {\"key1\" : \"Registro\",\"key2\" : \"" + email +"\"}\n" + 
					"  },\n" + 
					"    \"notification_target\" : {\n" + 
					"    \"type\" : \"devices_target\",\n" + 
					"    \"devices\" : [\"" + deviceToken + "\"]\n" + 
					"  }\n" + 
					"}");
			wr.flush();

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}
			result = "ok";
		} catch (Exception e) {
			e.printStackTrace();
			result = "ko";
		}
		System.out.println("GCM Notification is sent successfully");

		return result;
	}
	
	/*
	 * 
	 * ONESIGNAL
	 * 
	 * */
	public static String sendPushNotificationRecoveryAppOneSignal(String deviceToken, String platform, String nota, String titulo) throws IOException {
		try {
			String jsonResponse;

			URL url = new URL(API_ONESIGNAL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setUseCaches(false);
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			con.setRequestProperty("Authorization", "Key " + ONESIGNAL_REST_API_KEY);
			con.setRequestMethod("POST");

			String strJsonBody = "{"
					+ "\"app_id\": \"" + ONESIGNAL_APP_ID + "\","
					+ "\"include_subscription_ids\": [\"" + deviceToken + "\"],"
					+ "\"headings\": {\"en\": \"" + titulo + "\", \"es\": \"" + titulo + "\"},"
					+ "\"contents\": {\"en\": \"" + nota + "\", \"es\": \"" + nota + "\"},"
					+ "\"small_icon\": \"" + ONESIGNAL_ICON + "\","
					+ "\"large_icon\": \"" + ONESIGNAL_ICON + "\""
					+ "}";

			byte[] sendBytes = strJsonBody.getBytes("UTF-8");
			con.setFixedLengthStreamingMode(sendBytes.length);

			OutputStream outputStream = con.getOutputStream();
			outputStream.write(sendBytes);

			int httpResponse = con.getResponseCode();
			System.out.println("OneSignal Okeymas httpResponse: " + httpResponse);

			if (httpResponse >= HttpURLConnection.HTTP_OK && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
				Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
				jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
				scanner.close();
			} else {
				Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
				jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
				scanner.close();
			}
			System.out.println("OneSignal Okeymas jsonResponse: " + jsonResponse);

		} catch (Throwable t) {
			t.printStackTrace();
		}
		return "";
	}

	
	public static String sendBulkPushNotificationOneSignal(List<String> tokens, String mensaje, String titulo) throws IOException {
		int batchSize = 2000;
		for (int i = 0; i < tokens.size(); i += batchSize) {
			List<String> batch = tokens.subList(i, Math.min(i + batchSize, tokens.size()));

			StringBuilder idsJson = new StringBuilder("[");
			for (int j = 0; j < batch.size(); j++) {
				idsJson.append("\"").append(batch.get(j)).append("\"");
				if (j < batch.size() - 1) idsJson.append(",");
			}
			idsJson.append("]");

			String strJsonBody = "{"
					+ "\"app_id\": \"" + ONESIGNAL_APP_ID + "\","
					+ "\"include_subscription_ids\": " + idsJson + ","
					+ "\"headings\": {\"en\": \"" + titulo + "\", \"es\": \"" + titulo + "\"},"
					+ "\"contents\": {\"en\": \"" + mensaje + "\", \"es\": \"" + mensaje + "\"},"
					+ "\"small_icon\": \"" + ONESIGNAL_ICON + "\","
					+ "\"large_icon\": \"" + ONESIGNAL_ICON + "\""
					+ "}";

			try {
				URL url = new URL(API_ONESIGNAL);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.setUseCaches(false);
				con.setDoOutput(true);
				con.setDoInput(true);
				con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
				con.setRequestProperty("Authorization", "Key " + ONESIGNAL_REST_API_KEY);
				con.setRequestMethod("POST");

				byte[] sendBytes = strJsonBody.getBytes("UTF-8");
				con.setFixedLengthStreamingMode(sendBytes.length);
				con.getOutputStream().write(sendBytes);

				int httpResponse = con.getResponseCode();
				System.out.println("OneSignal bulk httpResponse (batch " + (i / batchSize + 1) + "): " + httpResponse);

				Scanner scanner = httpResponse >= HttpURLConnection.HTTP_OK && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST
						? new Scanner(con.getInputStream(), "UTF-8")
						: new Scanner(con.getErrorStream(), "UTF-8");
				System.out.println("OneSignal bulk jsonResponse: " + scanner.useDelimiter("\\A").next());
				scanner.close();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		return "";
	}

	private static String convertFromUTF8(String s) {
		String out = null;
		try {
			out = new String(s.getBytes("ISO-8859-1"), "UTF-8");
		} catch (java.io.UnsupportedEncodingException e) {
			return null;
		}
		return out;
	}
	
	public static String sendPushNotificationRecoveryAppOneSignalWiemspro(String deviceToken, String platform, String nota, String titulo, String senderId, String wiemssproRestApiKey) throws IOException {
		try {
			System.out.println("OneSignal Wiemspro: deviceToken: " + deviceToken + " , platform:" + platform);
			System.out.println("OneSignal Wiemspro: titulo: " + titulo + " , nota:" + nota);
			String sTitulo = convertFromUTF8(titulo);
			String sNota = convertFromUTF8(nota);

			String jsonResponse;

			URL url = new URL(API_ONESIGNAL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setUseCaches(false);
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			con.setRequestProperty("Authorization", "Key " + wiemssproRestApiKey);
			con.setRequestMethod("POST");

			String strJsonBody = "{"
					+ "\"app_id\": \"" + ONESIGNAL_APP_ID_WIEMSPRO + "\","
					+ "\"include_subscription_ids\": [\"" + deviceToken + "\"],"
					+ "\"data\": {\"fecha\": \"" + new Date() + "\", \"senderId\": \"" + senderId + "\"},"
					+ "\"headings\": {\"en\": \"" + sTitulo + "\", \"es\": \"" + sTitulo + "\"},"
					+ "\"contents\": {\"en\": \"" + sNota + "\", \"es\": \"" + sNota + "\"},"
					+ "\"small_icon\": \"" + ONESIGNAL_ICON + "\","
					+ "\"large_icon\": \"" + ONESIGNAL_ICON + "\""
					+ "}";

			System.out.println("OneSignal Wiemspro strJsonBody: " + strJsonBody);

			byte[] sendBytes = strJsonBody.getBytes("UTF-8");
			con.setFixedLengthStreamingMode(sendBytes.length);

			OutputStream outputStream = con.getOutputStream();
			outputStream.write(sendBytes);

			int httpResponse = con.getResponseCode();
			System.out.println("OneSignal Wiemspro httpResponse: " + httpResponse);

			if (httpResponse >= HttpURLConnection.HTTP_OK && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
				Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
				jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
				scanner.close();
			} else {
				Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
				jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
				scanner.close();
			}
			System.out.println("OneSignal Wiemspro jsonResponse: " + jsonResponse);

		} catch (Throwable t) {
			t.printStackTrace();
		}
		return "";
	}
	
	public static String sendPushNotificationRecoveryAppCenter(String deviceToken, String platform,String email) throws IOException {
		String result = "";
		URL url;
		if (platform.toLowerCase().equals("android")) {
			url=new URL(API_APP_CENTER_ANDORID);
		}else {
			url=new URL(API_APP_CENTER_IOS);
		}
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);


		conn.setRequestMethod("POST");
		conn.setRequestProperty("X-API-Token", AUTH_KEY_APP_CENTER);
		conn.setRequestProperty("Content-Type", "application/json");

		try {
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write("{\n" + 
					" \"notification_content\" : {\n" + 
					"  \"name\" : \"Pulse para cambiar su contraseña\",\n" + 
					"  \"title\" : \"Contraseña\",\n" + 
					"  \"body\" : \"Pulse para cambiar su contraseña\",\n" + 
					"  \"custom_data\" : {\"key1\" : \"CambioPassword\",\"key2\" : \"" + email +"\"}\n" + 
					"  },\n" + 
					"    \"notification_target\" : {\n" + 
					"    \"type\" : \"devices_target\",\n" + 
					"    \"devices\" : [\"" + deviceToken + "\"]\n" + 
					"  }\n" + 
					"}");
			wr.flush();

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}
			result = "ok";
		} catch (Exception e) {
			e.printStackTrace();
			result = "ko";
		}
		System.out.println("GCM Notification is sent successfully");

		return result;
	}
}
