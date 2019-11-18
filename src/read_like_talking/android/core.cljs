(ns read-like-talking.android.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [kitchen-async.promise :as p]
            [read-like-talking.events]
            [read-like-talking.subs]
            [read-like-talking.android.voice :as v]))

(def ^:private d (if goog.DEBUG (fn [e] (prn e) e) identity))

(def ReactNative (js/require "react-native"))

(def app-registry (.-AppRegistry ReactNative))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
#_(def image (r/adapt-react-class (.-Image ReactNative)))
(def button (r/adapt-react-class (.-TouchableHighlight ReactNative)))
(def back (.-BackHandler ReactNative))

#_(def logo-img (js/require "./images/cljs.png"))

(defn start-talking
  ([]
   (start-talking nil))
  ([keep]
   (p/do (dispatch [:start-talking keep])
         (v/talk!))))

(defn go-idling []
  (p/do (v/cancel!)
        (dispatch [:go-idling])))

(defn on-result [res]
  (let [res-v (v/result-v res)]
    (dispatch [:on-result res-v])))

(defn on-error [err]
  (let [[code desc] (d (v/error-v err))]
    (if (v/retryable-error? code)
      (v/talk!)
      (dispatch [:on-error code desc]))))

(def color-main "#336699")
(def color-sub "#ff6e5a")
(def color-sub-disable "#ff9a8c")
(def color-talk1 "#e9ffd3")
(def color-talk2 "#d9e5ff")
(def color-none "#ffffff00")

(comment
  (dispatch [:go-idling])
  (dispatch [:start-talking])
  (dispatch [:on-result ["yo" "hey" "hi"]])
  (dispatch [:on-error 7 "No match"])
  (dispatch [:on-error 8 "Unknown"])
  )

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
           [text {:style (text-style "#444" :padding 16)}
            "お話しください……"]
           [button {:style (button-style color-sub
                                         :margin-left 0
                                         :margin-right 0
                                         :border-radius 0)
                    :on-press #(start-talking)}
            [text {:style (text-style "white")}
             "🎤 会話を続ける 👂"]])]))))

(defn reading
  [s prim?]
  (let [fs (if prim? 36 28)
        mg (if prim? 24 26)
        bc (if prim? color-talk1 color-talk2)
        result-style (text-style "black"
                                 :flex-grow 1
                                 :font-size fs
                                 :background-color bc)]
    [view {:style {:flex-grow 1
                   :flex-direction "row"}}
     [text {:style result-style} s]
     [button {:style (assoc (button-style color-main)
                            :margin-top 4
                            :margin-bottom 4
                            :padding-left mg
                            :padding-right mg)
              :on-press #(start-talking s)}
      [text {:style (text-style "white" :flex-grow 1 :font-size fs)} "+"]]]))

(defn result-reading
  [talk keep]
  (let [prim (first talk)
        subs (take 3 (next talk))
        keep (if keep (str keep \newline) "")
        result-style (text-style "black" :flex-grow 1 :font-size 36)]
    [view {:style {:flex-grow 1
                   :flex-direction "column"}}
     [reading (str keep prim) true]
     [view {:style {:flex-grow 0
                    :flex-direction "column"}}
      (for [s subs]
        ^{:key s} [reading s false])]]))

(defn main-content []
  (let [status (subscribe [:status])
        talk (subscribe [:talk])
        keep (subscribe [:keep])
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
                   :on-press #(start-talking)}
           [text {:style (text-style "white"
                                     :padding-left 20 :padding-right 20)}
            "おしゃべり開始"]]]
         [result-reading @talk @keep]
         #_[view {:style {:flex-grow 1
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
                  :on-press #(go-idling)}
          [text {:style (text-style "white" :font-weight "bold")}
           "休憩する"]])
       [button {:style (button-style color-sub :flex-grow 1)
                :on-press #(.exitApp back)}
        [text {:style (text-style "white" :font-weight "bold")}
         "終わる"]]])))

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
  (v/init! on-result on-error)
  (.registerComponent app-registry
                      "ReadLikeTalking"
                      #(r/reactify-component app-root)))
