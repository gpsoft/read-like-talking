(ns read-like-talking.db
  (:require [clojure.spec.alpha :as s]))

;; spec of app-db
;; {:status :idling
;;  :talk ["yo" "ho" "no"]
;;  :last-error [7 "No Match"]}
(s/def ::status (s/and #{:idling :talking :reading :error}))
(s/def ::talk (s/and vector? (s/coll-of string?)))
(s/def ::keep (s/nilable string?))
(s/def ::last-error (s/cat :error-code int? :error-description string?))
(s/def ::app-db
  (s/keys :req-un [::status ::talk]
          :opt-un [::keep ::last-error]))

;; initial state of app-db
(def app-db {:status :idling
             :talk []})
