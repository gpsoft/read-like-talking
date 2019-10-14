(ns read-like-talking.events
  (:require
   [re-frame.core :refer [reg-event-db after]]
   [clojure.spec.alpha :as s]
   [read-like-talking.db :as db :refer [app-db]]))

;; -- Interceptors ------------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/blob/master/docs/Interceptors.md
;;
(defn check-and-throw
  "Throw an exception if db doesn't have a valid spec."
  [spec db [event]]
  (when-not (s/valid? spec db)
    (let [explain-data (s/explain-data spec db)]
      (throw (ex-info (str "Spec check after " event " failed: " explain-data) explain-data)))))

(def validate-spec
  (if goog.DEBUG
    (after (partial check-and-throw ::db/app-db))
    []))

;; -- Handlers --------------------------------------------------------------

(reg-event-db
 :initialize-db
 validate-spec
 (fn [_ _]
   app-db))

(reg-event-db
 :go-idling
 validate-spec
 (fn [db [_]]
   (assoc db :status :idling :talk [])))

(reg-event-db
  :start-talking
  validate-spec
  (fn [db [_]]
    (-> db
        (assoc :status :talking :talk [])
        (dissoc :last-error))))

(reg-event-db
  :on-result
  validate-spec
  (fn [db [_ value]]
    (-> db
        (assoc :status :reading :talk value)
        (dissoc :last-error))))

(reg-event-db
  :on-error
  validate-spec
  (fn [db [_ code desc]]
    (assoc db :status :reading :talk ["" desc])
    #_(assoc db :status :error :talk [] :last-error [code desc])))
