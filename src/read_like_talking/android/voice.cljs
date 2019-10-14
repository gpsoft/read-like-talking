(ns read-like-talking.android.voice
  (:require [kitchen-async.promise :as p]))

(defonce ^:private Voice (.-default (js/require "react-native-voice")))
(def ^:private err-code-timeout 6)
(def ^:private err-code-nomatch 7)

(defn init! [on-result on-error]
  (set! (.-onSpeechResults Voice) #(on-result %))
  (set! (.-onSpeechError Voice) #(on-error %)))

(defn- cancel! []
  (p/let [on? (.isRecognizing Voice)]
    (when on? (.cancel Voice))))

(defn idle! []
  (p/do (cancel!)))

(defn talk! []
  (p/do (cancel!)
        (.start Voice "ja_JP")))

(defn result-v
  [res]
  (js->clj (.-value res)))

(defn error-v
  [err]
  (let [err (.-error.message err)
        [code desc] (clojure.string.split err "/")
        code (int code)]
    [code desc]))

(defn retryable-error?
  [code]
  (#{err-code-timeout err-code-nomatch} code))

(comment
  (p/let [a (.isAvailable Voice)] (js/alert a))
  (p/let [a (.isRecognizing Voice)] (js/alert a))
  (p/let [a (.getSpeechRecognitionServices Voice)] (js/alert (js->clj a)))
  )
