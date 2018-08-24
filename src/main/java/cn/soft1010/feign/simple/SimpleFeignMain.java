package cn.soft1010.feign.simple;

import feign.*;

/**
 * Created by bjzhangjifu on 2018/8/23.
 */
public class SimpleFeignMain {

    public static void main(String[] args) {

        /**
         * 无参数直接请求
         */
        BAIDUAPI api = Feign.builder().target(BAIDUAPI.class, "https://www.baidu.com");
        System.out.println(api.index());


        /**
         * url包含参数
         */
        SougouAPI sougouAPI = Feign.builder().target(SougouAPI.class, "http://weixin.sogou.com/sugg/ajaj_json.jsp");
        String result = sougouAPI.sougou("feign", System.currentTimeMillis());
        System.out.println(result);

    }


    interface BAIDUAPI {
        @RequestLine("GET /")
        String index();
    }

    interface SougouAPI {
        @RequestLine("GET ?key={key}&type=wxart&pr=web&t={timestamp}")
        String sougou(@Param(value = "key") String key, @Param(value = "timestamp") long timestamp);
    }


}
