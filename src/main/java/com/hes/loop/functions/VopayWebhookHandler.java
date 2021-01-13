package com.hes.loop.functions;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;



public class VopayWebhookHandler implements HttpFunction {

    private static final Logger LOGGER = Logger.getLogger(VopayWebhookHandler.class.getName());
    private  Publisher PUBLISHER;


    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        String code = new String();

//        String header  = new String(Hex.decodeHex("0c90a153ad403dd8586a5eca6cd0b2f38412b8cb5454d52b241f90e3f7738a9a"),"UTF-8");
//       String loop =  Base64.getEncoder().encodeToString(header.getBytes());
//        System.out.println("Header: " + header);
//        String w = "{\n" +
//                "    \"ResponseType\": \"GetAccountsDetail\",\n" +
//                "    \"HttpStatusCode\": 200,\n" +
//                "    \"Accounts\": [\n" +
//                "        {\n" +
//                "            \"TransitNumber\": \"77777\",\n" +
//                "            \"InstitutionNumber\": \"777\",\n" +
//                "            \"OverdraftLimit\": 0,\n" +
//                "            \"Title\": \"Chequing CAD\",\n" +
//                "            \"AccountNumber\": \"1111000\",\n" +
//                "            \"Category\": \"Operations\",\n" +
//                "            \"Type\": \"Chequing\",\n" +
//                "            \"Currency\": \"CAD\",\n" +
//                "            \"Id\": \"ae1dac72-70da-4626-fed8-08d682e1ff4a\"\n" +
//                "        }\n" +
//                "],\n" +
//                "    \"Login\": {\n" +
//                "        \"Username\": \"Greatday\",\n" +
//                "        \"IsScheduledRefresh\": false,\n" +
//                "        \"LastRefresh\": \"2019-05-09T13:47:46.5227901\",\n" +
//                "        \"Type\": \"Personal\",\n" +
//                "        \"Id\": \"5e115eac-1209-4f19-641c-08d6d484e2fe\"\n" +
//                "    },\n" +
//                "    \"Institution\": \"FlinksCapital\",\n" +
//                "    \"RequestId\": \"1243c283-e0ca-4fda-a5e4-343068430190\"\n" +
//                "}";
//        byte[] hash = calcHmacSha256("key-alex".getBytes(), w.getBytes(StandardCharsets.US_ASCII));
//        String s = Base64.getEncoder().encodeToString(hash);
//        System.out.println(s);
//        InputStream inputStream = request.getInputStream();
//        byte[] req  = inputStream.readAllBytes();
//
//        boolean equals = new String(req).equals(w);
//        System.out.println(equals);


        this.PUBLISHER = Publisher.newBuilder( ProjectTopicName.of("pubsub-301312","alex")).build();
        InputStream inputStream = request.getInputStream();
        byte[] bytes = inputStream.readAllBytes();

        inputStream.close();
        byte[] callbackHash = calcHmacSha256("key-alex".getBytes(StandardCharsets.US_ASCII), bytes);

        var a = Base64.getEncoder().encodeToString(callbackHash);
        final PubsubMessage message = PubsubMessage.newBuilder().setData( ByteString.copyFrom(bytes)).build();
        final String messageId = PUBLISHER.publish(message).get();
        LOGGER.info("PubSub messageId: " + messageId);
        response.setStatusCode(200);
    }




    static public byte[] calcHmacSha256(byte[] secretKey, byte[] message) {
        byte[] hmacSha256 = null;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "HmacSHA256");
            mac.init(secretKeySpec);
            hmacSha256 = mac.doFinal(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate hmac-sha256", e);
        }
        return hmacSha256;
    }


}