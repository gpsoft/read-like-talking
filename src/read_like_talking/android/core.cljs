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
(def image (r/adapt-react-class (.-Image ReactNative)))
(def button (r/adapt-react-class (.-TouchableHighlight ReactNative)))
(def back (.-BackHandler ReactNative))

(def logo-img (js/require "./images/cljs.png"))

(def Voice (.-default (js/require "react-native-voice")))
(set! (.-onSpeechResults Voice) #(dispatch [:set-talk ""]))
(defn voice-talk []
  (p/do #_(.cancel Voice)
        #_(.start Voice "ja-JP")
        (dispatch [:set-status :talking])))
(defn voice-idle []
  (p/do #_(.cancel Voice)
        (dispatch [:set-status :idling])))
; (.start Voice "ja-JP")
; (p/let [a (.start Voice "ja-JP")] (js/alert "hey"))
(comment
  ; (.isAvailable Voice)
  (p/let [a (.start Voice "ja-JP")] (prn "hey"))
  (dispatch [:set-status :idling])
  (dispatch [:set-status :talking])
  (dispatch [:set-status :reading])
  (dispatch [:set-talk "今季は6月までの前半戦で3勝を挙げ、一時は独走態勢に。しかし、後半戦に入り持病の左手首痛が再発。さらに渋野の躍進で、前週の日本女子オープンまでに約860万円差まで詰め寄られた。"])
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
  (let [status (subscribe [:get-status])]
    (fn []
      (when (not= @status :idling)
        [view {:style {:flex-grow 0
                       :flex-direction "column"}}
         (if (= @status :talking)
           [text {:style (text-style "#444" :padding 16)} "お話しください……"]
           [button {:style (button-style color-sub :margin-left 0 :margin-right 0 :border-radius 0)
                    :on-press #(alert "HELLO!")}
            [text {:style (text-style "white")} "🎤 会話を続ける 👂"]])]))))

(defn main-content []
  (let [status (subscribe [:get-status])
        talk1 (subscribe [:get-talk1])
        talk2 (subscribe [:get-talk2])]
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
                   :on-press #(voice-talk)}
           [text {:style (text-style "white" :padding-left 20 :padding-right 20)} "おしゃべり開始"]]]
         [view {:style {:flex-grow 1
                        :flex-direction "column"}}
          [text {:style (text-style "black" :flex-grow 1 :font-size 36 :background-color color-talk1)} @talk1]
          [text {:style (text-style "black" :flex-grow 1 :font-size 36 :background-color color-talk2)} @talk2]])])))

(defn footer []
  (let [status (subscribe [:get-status])]
    (fn []
      [view {:style {:flex-grow 0
                     :flex-direction "row"
                     :padding-bottom 8}}
       (when (not= @status :idling)
         [button {:style (button-style color-sub :flex-grow 1)
                  :on-press #(voice-idle)}
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
    )
  #_(let [talk1 (subscribe [:get-talk1])
          talk2 (subscribe [:get-talk2])]
      (fn []
        [view {:style {:flex-direction "column"
                       :margin 20
                       :align-items "stretch"}}
         [button {:style {:background-color "#d75341"
                          :padding 16
                          :margin 20
                          :border-radius 4}
                  :on-press #(alert "HELLO!")}
          [text {:style {:color "white"
                         :font-size 24
                         :text-align "center"
                         :font-weight "bold"}}
           "聞いて〜"]]
         [text {:style {:background-color "#e9ffd3" :padding 12 :font-size 30 :font-weight "100" :margin-bottom 20 :text-align "center"}} @talk1]
         [text {:style {:background-color "#d9e5ff" :padding 12 :font-size 30 :font-weight "100" :margin-bottom 20 :text-align "center"}} @talk2]
         #_[image {:source logo-img
                   :style  {:width 80 :height 80 :margin-bottom 30}}]])))

(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "ReadLikeTalking" #(r/reactify-component app-root)))
