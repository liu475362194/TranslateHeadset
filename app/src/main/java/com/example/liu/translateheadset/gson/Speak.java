package com.example.liu.translateheadset.gson;

import java.util.List;

/**
 * Created by pzbz025 on 2017/11/16.
 */

public class Speak {

    /**
     * results_recognition : ["123456789"]
     * origin_result : {"corpus_no":6488940827917416667,"err_no":0,"result":{"word":["123456789"]},"sn":"76624b6c-e39b-4dd0-8611-183ecab6b2ca"}
     * error : 0
     * best_result : 123456789
     * result_type : partial_result
     *
     * {"results_recognition":["你好"],"origin_result":{"corpus_no":6489287693051647198,"err_no":0,"result":{"word":["你好"]},"sn":"abe96efe-77b9-409b-b5b7-3f24de755b75"},"error":0,"best_result":"你好","result_type":"final_result"}
     * {"results_recognition":["what one"],"origin_result":{"corpus_no":6489287970907705204,"err_no":0,"result":{"word":["what one"]},"sn":"e35c777c-d09e-4764-98b1-d5fa3b1975a8"},"error":0,"best_result":"what one","result_type":"final_result"}
     */

    private OriginResultBean origin_result;
    private int error;
    private String best_result;
    private String result_type;
    private List<String> results_recognition;

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

    public String getBest_result() {
        return best_result;
    }

    public void setBest_result(String best_result) {
        this.best_result = best_result;
    }

    public String getResult_type() {
        return result_type;
    }

    public void setResult_type(String result_type) {
        this.result_type = result_type;
    }

    public List<String> getResults_recognition() {
        return results_recognition;
    }

    public void setResults_recognition(List<String> results_recognition) {
        this.results_recognition = results_recognition;
    }

    public static class OriginResultBean {
        /**
         * corpus_no : 6488940827917416667
         * err_no : 0
         * result : {"word":["123456789"]}
         * sn : 76624b6c-e39b-4dd0-8611-183ecab6b2ca
         */

        private long corpus_no;
        private int err_no;
        private ResultBean result;
        private String sn;

        public long getCorpus_no() {
            return corpus_no;
        }

        public void setCorpus_no(long corpus_no) {
            this.corpus_no = corpus_no;
        }

        public int getErr_no() {
            return err_no;
        }

        public void setErr_no(int err_no) {
            this.err_no = err_no;
        }

        public ResultBean getResult() {
            return result;
        }

        public void setResult(ResultBean result) {
            this.result = result;
        }

        public String getSn() {
            return sn;
        }

        public void setSn(String sn) {
            this.sn = sn;
        }

        public static class ResultBean {
            private List<String> word;

            public List<String> getWord() {
                return word;
            }

            public void setWord(List<String> word) {
                this.word = word;
            }
        }
    }
}
