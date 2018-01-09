package com.example.liu.translateheadset.gson;

/**
 * Created by pzbz025 on 2017/11/17.
 */

public class Error {

    /**
     * origin_result : {"sn":"cuid=F9B1BC520A06D1C41DB22CE5E1208BD0|747186420967668&sn=45538ee9-1e71-47cf-a889-a85e900c64ea&nettype=4","error":2,"desc":"Network is not available","sub_error":2100}
     * error : 2
     * desc : Network is not available
     * sub_error : 2100
     */

    private OriginResultBean origin_result;
    private int error;
    private String desc;
    private int sub_error;

    public OriginResultBean getOrigin_result() {
        return origin_result;
    }

    public void setOrigin_result(OriginResultBean origin_result) {
        this.origin_result = origin_result;
    }

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getSub_error() {
        return sub_error;
    }

    public void setSub_error(int sub_error) {
        this.sub_error = sub_error;
    }

    public static class OriginResultBean {
        /**
         * sn : cuid=F9B1BC520A06D1C41DB22CE5E1208BD0|747186420967668&sn=45538ee9-1e71-47cf-a889-a85e900c64ea&nettype=4
         * error : 2
         * desc : Network is not available
         * sub_error : 2100
         */

        private String sn;
        private int error;
        private String desc;
        private int sub_error;

        public String getSn() {
            return sn;
        }

        public void setSn(String sn) {
            this.sn = sn;
        }

        public int getError() {
            return error;
        }

        public void setError(int error) {
            this.error = error;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public int getSub_error() {
            return sub_error;
        }

        public void setSub_error(int sub_error) {
            this.sub_error = sub_error;
        }
    }
}
