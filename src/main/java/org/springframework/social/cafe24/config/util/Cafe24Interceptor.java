package org.springframework.social.cafe24.config.util;


import com.cafe24.devbit004.pop.social.api.Cafe24;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.connect.web.ConnectInterceptor;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.WebRequest;


public class Cafe24Interceptor implements ConnectInterceptor<Cafe24> {
    private static final Logger logger = LoggerFactory.getLogger(Cafe24Interceptor.class);


    @Override
    public void preConnect(ConnectionFactory<Cafe24> connectionFactory, MultiValueMap<String, String> parameters, WebRequest request) {
        logger.debug("preConnect!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        /*logger.debug("preConnect mall_id: " + request.getParameter("mall_id"));
        String mallId = request.getParameter("mall_id");
        parameters.add("mall_id", mallId);
        String teamName = "now2fix";
        JsonStringEncoder jsonStringEncoder = JsonStringEncoder.getInstance();
        byte[] stateByteArr = jsonStringEncoder.encodeAsUTF8(mallId + teamName);
        logger.info("preConnect stateByteArr: " + stateByteArr.toString());

//        JsonEncoding.valueOf(mallId + teamName);
        byte[] state = Base64Utils.encode(stateByteArr);
        logger.info("preConnect state: " + state.toString());

        parameters.add("state", state.toString());*/


    }

    @Override
    public void postConnect(Connection<Cafe24> connection, WebRequest request) {
        logger.info("postConnect!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        /*Cafe24Template cafe24Template = (Cafe24Template) connection;
        ProductTemplate productTemplate = cafe24Template.productTemplate();
        List<Product> productList = productTemplate.getProducts();
        if (productList != null) {
            for (Product product : productList) {
                logger.info("product getProductName: " + product.getProductName());
                logger.info("product getProductName: " + product.getProductCode());
                logger.info("product getProductName: " + product.getShopId());
            }
        } else {
            logger.info("productList 가져오기 실패");

        }*/
    }
}
