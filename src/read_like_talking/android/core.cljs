(ns read-like-talking.android.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [kitchen-async.promise :as p]
            [read-like-talking.events]
            [read-like-talking.subs]))

(def ReactNative (js/require "react-native"))

(def app-registry (.-AppRegistry ReactNative))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
#_(def image (r/adapt-react-class (.-Image ReactNative)))
(def button (r/adapt-react-class (.-TouchableHighlight ReactNative)))
(def back (.-BackHandler ReactNative))

#_(def logo-img (js/require "./images/cljs.png"))

(def Voice (.-default (js/require "react-native-voice")))
(def err-code-timeout 6)
(def err-code-nomatch 7)

(defn voice-idle []
  (p/do (.cancel Voice)
        (dispatch [:go-idling])))

(defn voice-talk []
  (p/do (.cancel Voice)
        (dispatch [:start-talking])
        (.start Voice "ja_JP")))

(defn voice-result [res]
  (let [res-v (js->clj (.-value res))]
    (dispatch [:on-result res-v])))

(defn voice-error [err]
  (let [err (.-error.message err)
        [code msg] (clojure.string.split err "/")
        code (int code)]
    (if (#{err-code-timeout err-code-nomatch} code)
      (voice-talk)
      (dispatch [:on-error code msg]))))

(defn voice-init []
  (set! (.-onSpeechResults Voice) #(voice-result %))
  (set! (.-onSpeechError Voice) #(voice-error %)))

(comment
  (p/let [a (.isAvailable Voice)] (js/alert a))
  (p/let [a (.isRecognizing Voice)] (js/alert a))
  (p/let [a (.getSpeechRecognitionServices Voice)] (js/alert (js->clj a)))
  (dispatch [:go-idling])
  (dispatch [:start-talking])
  (dispatch [:on-result ["yo" "hey" "hi"]])
  (dispatch [:on-error 7 "No match"])
  (dispatch [:on-error 8 "Unknown"])
  )

(def color-main "#336699")
(def color-sub "#ff6e5a")
(def color-sub-disable "#ff9a8c")
(def color-talk1 "#e9ffd3")
(def color-talk2 "#d9e5ff")
(def color-none "#ffffff00")

(defn text-style [color & kvs]
  (let [m {:color color
           :font-size 24
           :text-align "center"
           :text-align-vertical "center"}]
    (if kvs (apply assoc m kvs) m)))

(defn button-style [color & kvs]
  (let [m {:background-color color
           :padding 16
           :margin-left 8
           :margin-right 8
           :border-radius 4}]
    (if kvs (apply assoc m kvs) m)))

(defn alert [title]
      (.alert (.-Alert ReactNative) title))

(defn header []
  (let [status (subscribe [:status])]
    (fn []
      (when (not= @status :idling)
        [view {:style {:flex-grow 0
                       :flex-direction "column"}}
         (if (= @status :talking)
           [text {:style (text-style "#444" :padding 16)} "お話しください……"]
           [button {:style (button-style color-sub
                                         :margin-left 0
                                         :margin-right 0 :border-radius 0)
                    :on-press voice-talk}
            [text {:style (text-style "white")} "🎤 会話を続ける 👂"]])]))))

(defn main-content []
  (let [status (subscribe [:status])
        talk (subscribe [:talk])
        result-style (text-style "black" :flex-grow 1 :font-size 36)]
    (fn []
      [view {:style {:flex-grow 1
                     :flex-direction "column"
                     :margin 8
                     :justify-content "center"}}

       (if (= @status :idling)
         [view {:style {:flex-grow 1
                        :flex-direction "column"
                        :align-items "center"
                        :justify-content "center"}}
          [button {:style (button-style color-main)
                   :on-press voice-talk}
           [text {:style (text-style "white" :padding-left 20 :padding-right 20)}
            "おしゃべり開始"]]]
         [view {:style {:flex-grow 1
                        :flex-direction "column"}}
          [text {:style (assoc result-style :background-color color-talk1)}
           (first @talk)]
          [text {:style (assoc result-style :background-color color-talk2
                               :font-size 28)}
           (clojure.string/join \newline (next @talk))]])])))

(defn footer []
  (let [status (subscribe [:status])]
    (fn []
      [view {:style {:flex-grow 0
                     :flex-direction "row"
                     :padding-bottom 8}}
       (when (not= @status :idling)
         [button {:style (button-style color-sub :flex-grow 1)
                  :on-press voice-idle}
          [text {:style (text-style "white" :font-weight "bold")} "休憩する"]])
       [button {:style (button-style color-sub :flex-grow 1)
                :on-press #(.exitApp back)}
        [text {:style (text-style "white" :font-weight "bold")} "終わる"]]])))

(defn app-root []
  (fn []
    [view {:style {:flex-grow 1
                   :flex-direction "column"
                   :align-items "stretch"}}
     [header]
     [main-content]
     [footer]]
    ))

(defn init []
  (dispatch-sync [:initialize-db])
  (voice-init)
  (.registerComponent app-registry "ReadLikeTalking" #(r/reactify-component app-root)))
