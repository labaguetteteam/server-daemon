(ns server-daemon.core
  (:gen-class)
  (:require [server-daemon.channels :as channels]
            [server-daemon.dynamodb :as dynamodb]
            [server-daemon.pipe :as pipe-file]
            [clojure.tools.cli :refer [parse-opts]]))

(def cli-options
  [["-s" "--serverToken TOKEN" "Server Token"]
   ["-a" "--awsAccessKey TOKEN" "AWS Access Key"]
   ["-b" "--awsSecretKey TOKEN" "AWS Secret Key"]
   ["-e" "--awsEndpoint URL" "AWS Endpoint"]
   ["-w" "--weatherAPIKey TOKEN" "Open Weather API Key"]
   ["-p" "--prod" "Prod Mode"]])

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]

    (channels/process-channel pipe-file/pipe)
    (dynamodb/init-database options {:start_latitude 1.279890
                                     :start_longitude 103.854869
                                     :end_latitude 1.314498
                                     :end_longitude 103.888652
                                     :seat_count 2})
    (loop [] (recur))))
