(ns server-daemon.weather
  (:require [org.httpkit.client :as http]
            [server-daemon.pipe :as pipe-file]
            [clojure.core.async :refer [go-loop go chan put! <!]]
            [clojure.data.json :as json]))

(defn get-weather
  "Call the Open Weather map API to get the weather"
  [options api-options]

  (println options)
  (http/get (str "http://api.openweathermap.org/data/2.5/weather?lat="(:start_latitude api-options)"&lon="(:start_longitude api-options)"&APPID="(:weatherAPIKey options))
          (fn [{:keys [status headers body error]}]
            (if error
              (println "[ERROR] Exception is " error)
              (do
                (println "[HTTP] GET : " status)
                (println "[BODY] : " body)
                (let [api-result (json/read-str body
                                                :key-fn keyword)]
                  (put! pipe-file/pipe [{:message :weather-api-result
                                         :options options
                                         :api-options api-options
                                         :value api-result}])))))))
