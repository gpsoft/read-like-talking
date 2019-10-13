(ns read-like-talking.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  :get-status
  (fn [db _]
    (:status db)))

(reg-sub
  :get-talk
  (fn [db _]
    (:talk db)))
