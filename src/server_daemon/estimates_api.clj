(ns server-daemon.estimates-api
  (:gen-class)
  (:require [org.httpkit.client :as http]
            [clojure.data.json :as json]))

(defn generate-options
  "Generate Options for the Uber API"
  [server-token]
  {:query-params {:start_latitude 1.279890
                  :start_longitude 103.854869
                  :end_latitude 1.314498
                  :end_longitude 103.888652
                  :seat_count 2}
   :headers {"Authorization" (str "Token " server-token)}})

(defn get-price
  "Call the Uber API to get the price"
  [options]
  (println "[RUN] estimates/get-price... ")
  (println "[PARAMS] Server Token : " (:serverToken options))
  (http/get "https://api.uber.com/v1/estimates/price" (generate-options (:serverToken options))
          (fn [{:keys [status headers body error]}]
            (if error
              (println "[ERROR] Exception is " error)
              (do
                (println "[HTTP] GET : " status)
                (println "[BODY] : " (json/read-str body)))))))
