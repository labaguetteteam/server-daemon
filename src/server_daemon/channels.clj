(ns server-daemon.channels
  (:require [clojure.core.async :refer [go-loop go chan put! <!]]))

(defmulti process-message
  (fn [[x]] (:message x)))

(defmethod process-message :surge_multiplier
  [[x]]
  (println "[SURGE_MULTIPLIER] : " (:value x)))

(defn process-channel [channel]
  (go-loop []
      (when-let [e (<! channel)]
        (process-message e)
        (recur))))

(def pipe (chan))
