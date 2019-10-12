(ns read-like-talking.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  :get-status
  (fn [db _]
    (:status db)))

(reg-sub
  :get-talk1
  (fn [db _]
    (:talk1 db)))

(reg-sub
  :get-talk2
  (fn [db _]
    (:talk2 db)))
