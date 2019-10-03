/**
 * Copyright © 2014-2016 Paolo Simonetto
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ocotillo.samples.parsers;

import java.awt.Color;
import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ocotillo.dygraph.DyEdgeAttribute;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.dygraph.Evolution;
import ocotillo.dygraph.FunctionConst;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Interval;
import ocotillo.graph.Edge;
import ocotillo.graph.Node;
import ocotillo.graph.StdAttribute;
import ocotillo.samples.parsers.Commons.DyDataSet;
import ocotillo.samples.parsers.Commons.Mode;
import ocotillo.serialization.ParserTools;

/**
 * Parses the rugby twitter data set.
 */
public class RugbyTweets {

    /**
     * An rugby tweet.
     */
    public static class Tweet {

        public final LocalDateTime time;
        public final String from;
        public final String to;

        private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        public Tweet(String time, String from, String to) {
            this.time = LocalDateTime.parse(time, timeFormatter);
            this.from = from;
            this.to = to;
        }
    }

    /**
     * The tweet dataset.
     */
    public static class TweetDataSet {

        public final List<Tweet> tweets;
        public final Set<String> teams;
        public final LocalDateTime firstTime;
        public final LocalDateTime lastTime;

        public TweetDataSet(List<Tweet> tweets, Set<String> teams, LocalDateTime firstTime, LocalDateTime lastTime) {
            this.tweets = tweets;
            this.teams = teams;
            this.firstTime = firstTime;
            this.lastTime = lastTime;
        }
    }

    /**
     * Produces the dynamic dataset for this data.
     *
     * @param mode the desired mode.
     * @return the dynamic dataset.
     */
    public static DyDataSet parse(Mode mode) {
        File file = new File("data/Rugby_tweets/pro12_mentions.csv");
        TweetDataSet dataset = parseTweets(file);
        return new DyDataSet(
                parseGraph(file, Duration.ofDays(1), mode),
                1.0 / Duration.ofDays(5).getSeconds(),
                Interval.newClosed(
                        dataset.firstTime.toEpochSecond(ZoneOffset.UTC),
                        dataset.lastTime.toEpochSecond(ZoneOffset.UTC)));
    }

    /**
     * Parses the rugby tweet dataset.
     *
     * @param file the file.
     * @return the tweet dataset.
     */
    public static TweetDataSet parseTweets(File file) {
        List<String> lines = ParserTools.readFileLines(file);
        Set<String> teams = new HashSet<>();
        List<Tweet> tweets = new ArrayList<>(lines.size() - 1);
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] tokens = line.split(",");
            tweets.add(new Tweet(tokens[0], tokens[1], tokens[2]));
            teams.add(tokens[1]);
            teams.add(tokens[2]);
        }
        return new TweetDataSet(tweets, teams, tweets.get(0).time, tweets.get(tweets.size() - 1).time);
    }

    /**
     * Parses the rugby tweet graph.
     *
     * @param file the file.
     * @param tweetDuration the duration assign to each tweet.
     * @param mode the desired mode.
     * @return the dynamic graph.
     */
    public static DyGraph parseGraph(File file, Duration tweetDuration, Mode mode) {
        DyGraph graph = new DyGraph();
        DyNodeAttribute<Boolean> presence = graph.nodeAttribute(StdAttribute.dyPresence);
        DyNodeAttribute<String> label = graph.nodeAttribute(StdAttribute.label);
        DyNodeAttribute<Coordinates> position = graph.nodeAttribute(StdAttribute.nodePosition);
        DyNodeAttribute<Color> color = graph.nodeAttribute(StdAttribute.color);
        DyEdgeAttribute<Boolean> edgePresence = graph.edgeAttribute(StdAttribute.dyPresence);
        DyEdgeAttribute<Color> edgeColor = graph.edgeAttribute(StdAttribute.color);
        long halfDuration = tweetDuration.dividedBy(2).getSeconds();

        TweetDataSet dataset = parseTweets(file);
        Map<String, Node> nodeMap = new HashMap<>();
        for (String team : dataset.teams) {
            Node node = graph.newNode(team);
            presence.set(node, new Evolution<>(false));
            label.set(node, new Evolution<>(team));
            position.set(node, new Evolution<>(new Coordinates(0, 0)));
            color.set(node, new Evolution<>(new Color(141, 211, 199)));
            nodeMap.put(team, node);
        }

        for (Tweet tweet : dataset.tweets) {
            Node source = nodeMap.get(tweet.from);
            Node target = nodeMap.get(tweet.to);
            Edge edge = graph.betweenEdge(source, target);
            if (edge == null) {
                edge = graph.newEdge(source, target);
                edgePresence.set(edge, new Evolution<>(false));
                edgeColor.set(edge, new Evolution<>(Color.BLACK));
            }

            Interval tweetInterval = Interval.newRightClosed(
                    tweet.time.minusSeconds(halfDuration).toEpochSecond(ZoneOffset.UTC),
                    tweet.time.plusSeconds(halfDuration).toEpochSecond(ZoneOffset.UTC));

            presence.get(source).insert(new FunctionConst<>(tweetInterval, true));
            presence.get(target).insert(new FunctionConst<>(tweetInterval, true));
            edgePresence.get(edge).insert(new FunctionConst<>(tweetInterval, true));
        }

        double startTime = dataset.firstTime.toEpochSecond(ZoneOffset.UTC);
        double endTime = dataset.lastTime.toEpochSecond(ZoneOffset.UTC);
        Commons.scatterNodes(graph, 100);
        Commons.mergeAndColor(graph, startTime - halfDuration, endTime + halfDuration, mode, new Color(141, 211, 199), Color.BLACK, halfDuration);
        return graph;
    }
}
