package cn.soft1010.feign.simple;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import feign.*;
import feign.codec.Decoder;
import feign.codec.EncodeException;
import feign.codec.Encoder;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bjzhangjifu on 2018/8/24.
 */
public class ComplexFeignMain {
    public static void main(String[] args) {

        Gson gson = new GsonBuilder().serializeNulls().create();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("page", 1);
        params.put("pageSize", 10);
        String body = gson.toJson(params);
        /**
         * 包含更多
         * 拦截器 编码 解码 options(超时时间) 重试 日志
         */
        List<RequestInterceptor> requestInterceptors = new ArrayList<RequestInterceptor>();
        requestInterceptors.add(new MyRequestInterceptor());
        TestHeader testHeader2 = Feign.builder().
                requestInterceptors(requestInterceptors).
                decoder(new MyDecoder()).
                encoder(new MyEncoder()).
                options(new Request.Options(10000, 60000)).
                retryer(new MyRetryer(1000, 10000, 3)).
                logger(new MyLogger()).
                logLevel(Logger.Level.BASIC).
                target(TestHeader.class, "http://crm3.netease.com");
        System.out.println(testHeader2.test3("qFnUFs5k__3NRAEjjqCK5Y9vCV4-i8H7XEYqIkqymb3zjeOjfQK47thg1C7DRpp1ifHt4vFoNr0E9oaQbJC3iw", body));
    }


    static class MyRequestInterceptor implements RequestInterceptor {
        @Override
        public void apply(RequestTemplate template) {
            System.out.println("MyRequestInterceptor  " + System.currentTimeMillis() + " 发起请求" + template.url() + " " + new String(template.body()));
        }
    }

    static class MyEncoder extends Encoder.Default {
        public void encode(Object o, Type type, RequestTemplate requestTemplate) throws EncodeException {
            System.out.println("MyEncoder " + type.toString() + "  " + o.toString());
        }
    }

    static class MyDecoder extends Decoder.Default {
        public Object decode(Response response, Type type) throws IOException, FeignException {
            if (response.status() == 200) {
                System.out.println("MyDecoder 请求成功");
            }
            return super.decode(response, type);
        }
    }

    static class MyLogger extends Logger {
        @Override
        protected void log(String configKey, String format, Object... args) {
            System.out.println(String.format("MyLogger " + methodTag(configKey) + format + "%n", args));
        }

        @Override
        protected void logRequest(String configKey, Level logLevel, Request request) {
            if (logLevel.equals(Level.BASIC)) {
                super.logRequest(configKey, logLevel, request);
            }
        }
    }

    static class MyRetryer implements Retryer {

        int maxAttempts;
        long period;
        long maxPeriod;
        int attempt;
        long sleptForMillis;

        public MyRetryer(long period, long maxPeriod, int maxAttempts) {
            this.period = period;
            this.maxAttempts = maxAttempts;
            this.maxPeriod = maxPeriod;
        }

        protected long currentTimeMillis() {
            return System.currentTimeMillis();
        }

        public void continueOrPropagate(RetryableException e) {
            if (this.attempt++ >= this.maxAttempts) {
                throw e;
            } else {
                long interval;
                if (e.retryAfter() != null) {
                    interval = e.retryAfter().getTime() - this.currentTimeMillis();
                    if (interval > this.maxPeriod) {
                        interval = this.maxPeriod;
                    }

                    if (interval < 0L) {
                        return;
                    }
                } else {
                    interval = this.nextMaxInterval();
                }

                try {
                    Thread.sleep(interval);
                } catch (InterruptedException var5) {
                    Thread.currentThread().interrupt();
                }

                this.sleptForMillis += interval;
            }
        }

        long nextMaxInterval() {
            long interval = (long) ((double) this.period * Math.pow(1.5D, (double) (this.attempt - 1)));
            return interval > this.maxPeriod ? this.maxPeriod : interval;
        }

        public Retryer clone() {
            System.out.println("MyRetryer clone");
            return new MyRetryer(this.period, this.maxPeriod, this.maxAttempts);
        }
    }
}
