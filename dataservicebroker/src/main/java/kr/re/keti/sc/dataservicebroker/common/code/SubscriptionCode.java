package kr.re.keti.sc.dataservicebroker.common.code;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public class SubscriptionCode {

	public static enum Timerel {
        @JsonProperty("before")
        BEFORE("before"),
        @JsonProperty("after")
        AFTER("after"),
        @JsonProperty("between")
        BETWEEN("between"),
    	;

    	private String code;

        private Timerel(String code) {
            this.code = code;
        }

        @JsonCreator
        public String getCode() {
            return code;
        }

        @JsonValue
        public static Timerel parseType(String code) {
            for (Timerel timerel : values()) {
                if (timerel.getCode().equals(code)) {
                    return timerel;
                }
            }
            return null;
        }
    }

    public static enum Status {

        @JsonProperty("active")
    	ACTIVE("active"),
        @JsonProperty("paused")
        PAUSED("paused"),
        @JsonProperty("expired")
        EXPIRED("expired"),
    	;

    	private String code;

        private Status(String code) {
            this.code = code;
        }

        @JsonCreator
        public String getCode() {
            return code;
        }

        @JsonValue
        public static Status parseType(String code) {
            for (Status timerel : values()) {
                if (timerel.getCode().equals(code)) {
                    return timerel;
                }
            }
            return null;
        }
    }

    public static enum Active {


        @JsonProperty("True")
        TRUE("True"),
        @JsonProperty("False")
        FALSE("False"),
    	;

    	private String code;

        private Active(String code) {
            this.code = code;
        }

        @JsonCreator
        public String getCode() {
            return code;
        }

        @JsonValue
        public static Active parseType(String code) {
            for (Active active : values()) {
                if (active.getCode().equals(code)) {
                    return active;
                }
            }
            return null;
        }
    }

    public static enum TriggerReason {

    	NEWLY_MATCHING("newlyMatching"),
    	UPDATED("updated"),
    	NO_LONGER_MATCHING("noLongerMatching"),
    	;

    	private String code;

        private TriggerReason(String code) {
            this.code = code;
        }

        @JsonCreator
        public String getCode() {
            return code;
        }
    }
}
