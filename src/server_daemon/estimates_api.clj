(ns server-daemon.estimates-api
  (:gen-class)
  (:require [org.httpkit.client :as http]
            [server-daemon.pipe :as pipe-file]
            [clojure.core.async :refer [go-loop go chan put! <!]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]))

(defn generate-options
  "Generate Options for the Uber API"
  [server-token api-options]
  {:query-params {:start_latitude (:start_latitude api-options)
                  :start_longitude (:start_longitude api-options)
                  :end_latitude (:end_latitude api-options)
                  :end_longitude (:end_longitude api-options)
                  :seat_count (:seat_count api-options)}
   :headers {"Authorization" (str "Token " server-token)}})

(defn find-uberx-surge-multiplier [index item]
  (when (= "uberX" (:display_name item))
    (:surge_multiplier item)))

(defn get-price
  "Call the Uber API to get the price"
  [options api-options weather]
  (log/info "[RUN] estimates/get-price... ")
  (log/info "[PARAMS] Server Token : " (:serverToken options))
  (http/get "https://api.uber.com/v1/estimates/price" (generate-options (:serverToken options) api-options)
          (fn [{:keys [status headers body error]}]
            (if error
              (log/info "[ERROR] Exception is " error)
              (do
                (log/info "[HTTP] GET : " status)
                (log/info "[BODY] : " body)
                (let [api-result (json/read-str body
                                                :key-fn keyword)]
                  (put! pipe-file/pipe [{:message :estimates-api-result
                                         :options options
                                         :api-options api-options
                                         :weather weather
                                         :value (get-in api-result [:prices])}])))))))
