(ns read-like-talking.db
  (:require [clojure.spec.alpha :as s]))

;; spec of app-db
(s/def ::talk1 string?)
(s/def ::talk2 string?)
(s/def ::app-db
  (s/keys :req-un [::talk1 ::talk2]))

;; initial state of app-db
(def app-db {:status :idling
             :talk1 ""
             :talk2 ""})
