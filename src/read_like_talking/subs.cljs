(ns read-like-talking.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  :status
  (fn [db _]
    (:status db)))

(reg-sub
  :talk
  (fn [db _]
    (:talk db)))

(reg-sub
  :keep
  (fn [db _]
    (:keep db)))

(reg-sub
  :last-error
  (fn [db _]
    (:last-error db)))
