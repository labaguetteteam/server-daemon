(ns server-daemon.channels
  (:require [clojure.core.async :refer [go-loop go chan put! <!]]
            [server-daemon.estimates-api :as estimates-api]
            [server-daemon.dynamodb :as dynamodb]
            [server-daemon.weather :as weather]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]))

(use 'overtone.at-at)

(def pool-task (mk-pool))

(defn process-get-data [options api-options]
  (weather/get-weather options api-options))

(defmulti process-message
  (fn [[x]] (:message x)))

(defmethod process-message :database-ready
  [[x]]
  (if (:options :prod x)
    (every 120000 #(process-get-data (:options x) (:api-options x)) pool-task)
    (do
      (process-get-data (:options x) (:api-options x)))))

(defmethod process-message :weather-api-result
  [[x]]
  (let [api-options (:api-options x)
        options (:options x)
        api-result (:value x)
        weather (get-in api-result [:weather 0 :description])]
    (log/info "[WEATHER]" weather)
    (estimates-api/get-price options api-options weather)))

(defmethod process-message :estimates-api-result
  [[x]]
  (let [api-options (:api-options x)
        weather (:weather x)
        surge (map-indexed
               (fn [index item] (when (= "uberX" (:display_name item))
                                  (:surge_multiplier item)))
               (:value x))
        estimates-pool (map-indexed
                        (fn [index item] (when (= "uberPOOL" (:display_name item))
                                           (:estimate item)))
                        (:value x))
        estimates-x (map-indexed
                     (fn [index item] (when (= "uberX" (:display_name item))
                                        (:estimate item)))
                     (:value x))
        duration (map-indexed
                  (fn [index item] (when (= "uberX" (:display_name item))
                                     (:duration item)))
                  (:value x))]
    (log/info "[SURGE_MULTIPLIER] : " (first (filter some? surge)))
    (log/info "[DURATION] : " (first (filter some? duration)))
    (log/info "[ESTIMATION UBER POOL] : " (first (filter some? estimates-pool)))
    (log/info "[ESTIMATION UBER X] : " (first (filter some? estimates-x)))
    (dynamodb/save-price (:options x) {:surge_multiplier (first (filter some? surge))
                                       :duration (first (filter some? duration))
                                       :estimation_pool (first (filter some? estimates-pool))
                                       :estimation_x (first (filter some? estimates-x))
                                       :start_latitude (:start_latitude api-options)
                                       :start_longitude (:start_longitude api-options)
                                       :end_latitude (:end_latitude api-options)
                                       :end_longitude (:end_longitude api-options)
                                       :seat_count (:seat_count api-options)
                                       :weather weather})))

(defn process-channel [channel]
  (go-loop []
    (when-let [e (<! channel)]
      (process-message e)
      (recur))))
