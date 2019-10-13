(ns read-like-talking.db
  (:require [clojure.spec.alpha :as s]))

;; spec of app-db
(s/def ::status keyword?)
(s/def ::talk vector?)
(s/def ::app-db
  (s/keys :req-un [::status ::talk]))

;; initial state of app-db
(def app-db {:status :idling
             :talk []})
