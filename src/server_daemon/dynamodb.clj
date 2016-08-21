(ns server-daemon.dynamodb
  (:require [server-daemon.pipe :as pipe-file]
            [server-daemon.tools :as tools]
            [org.httpkit.client :as http]
            [clojure.core.async :refer [go-loop go chan put! <!]]
            [clojure.tools.logging :as log])
  (:use [amazonica.aws.dynamodbv2]))

(defn cred [options] {:access-key (:awsAccessKey options)
                      :secret-key (:awsSecretKey options)
                      :endpoint   (:awsEndpoint options)})

(defn init-database [options api-options]

  (try
    (create-table (cred options)
                  :table-name "UberMachineLearning"
                  :key-schema
                  [{:attribute-name "id"   :key-type "HASH"}
                   {:attribute-name "date" :key-type "RANGE"}]
                  :attribute-definitions
                  [{:attribute-name "id"      :attribute-type "S"}
                   {:attribute-name "date"    :attribute-type "N"}
                   {:attribute-name "estimation_pool" :attribute-type "S"}
                   {:attribute-name "estimation_x" :attribute-type "S"}]
                  :local-secondary-indexes
                  [{:index-name "estimation_pool_idx"
                    :key-schema
                    [{:attribute-name "id"   :key-type "HASH"}
                     {:attribute-name "estimation_pool" :key-type "RANGE"}]
                    :projection
                    {:projection-type "INCLUDE"
                     :non-key-attributes ["id" "date" "estimation_pool" "surge_multiplier"]}}
                   {:index-name "estimation_x_idx"
                    :key-schema
                    [{:attribute-name "id"   :key-type "HASH"}
                     {:attribute-name "estimation_x" :key-type "RANGE"}]
                    :projection
                    {:projection-type "INCLUDE"
                     :non-key-attributes ["id" "date" "estimation_x" "surge_multiplier"]}}]
                  :provisioned-throughput
                  {:read-capacity-units 1
                   :write-capacity-units 1})
    (catch Exception e (log/info "[INFORMATION] The table already exist")))

  (time (Thread/sleep 10000))
  (put! pipe-file/pipe [{:message :database-ready
                         :options options
                         :api-options api-options}]))

(defn save-price [options api-result]

  (put-item (cred options)
            :table-name "UberMachineLearning"
            :return-consumed-capacity "TOTAL"
            :return-item-collection-metrics "SIZE"
            :item {
                   :id (tools/uuid)
                   :date (tools/timestamp)
                   :surge_multiplier (str (:surge_multiplier api-result))
                   :duration (str (:duration api-result))
                   :estimation_pool (str (:estimation_pool api-result))
                   :estimation_x (str (:estimation_x api-result))
                   :weather (str (:weather api-result))
                   :start_longitude (str (:start_longitude api-result))
                   :start_latitude (str (:start_latitude api-result))
                   :end_longitude (str (:end_longitude api-result))
                   :end_latitude (str (:end_latitude api-result))
                   :seat_count (str (:seat_count api-result))
                   }))
