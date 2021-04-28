import io.netty.handler.codec.http.cookie.Cookie;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.asynchttpclient.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import static org.asynchttpclient.Dsl.*;

public class Main {

    private static List<Game> games = new LinkedList<>();

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

        //init all leagues
        List<String> leaguesList = getLeaguesLinks("https://solobet15.com/sport/FB?single=true");
        leaguesList.addAll(getLeaguesLinks("https://solobet15.com/sport/T?single=true"));
        leaguesList.addAll(getLeaguesLinks("https://solobet15.com/sport/BBL?single=true"));

        try (AsyncHttpClient asyncHttpClient = asyncHttpClient()) {

            asyncHttpClient
                    .preparePost("https://solobet15.com/Service/SetSprachkz")
                    .addQueryParam("sprachkz:", "\"E\"")
                    .execute()
                    .toCompletableFuture()
                    .thenApply(response -> {
                        getGames(response.getResponseBody());
                        return response;
                    })
                    .join();

            for (String leagueLink : leaguesList) {

                asyncHttpClient
                        .prepareGet("https://solobet15.com/" + leagueLink)
                        .execute()
                        .toCompletableFuture()
                        .thenApply(response -> {
                            getGames(response.getResponseBody());
                            return response;
                        })
                        .join();

                while (games.size() > 0) {
                    Game g = games.get(0);
                    asyncHttpClient
                            .prepareGet("https://solobet15.com/Sports/specialbets?MatchId=" + g.getId() + "&quicktip=false&popup=false")
                            .execute()
                            .toCompletableFuture()
                            .thenApply(response -> {
                                parsePrintAndRemoveGame(g, response.getResponseBody());
                                return response;
                            })
                            .thenAccept(r -> System.out.println())
                            .join();

                }

            }
        }
    }

    private static List<String> getLeaguesLinks(String url) throws IOException {
        List<String> result = new LinkedList<>();
        Document doc = Jsoup.connect(url).get();

        for (Element ref : doc.getElementsByClass("league-items").get(0).getElementsByTag("a")) {
            result.add(ref.attr("href"));
        }
        return result;
    }

    private static void getGames(String html) {
        Document doc = Jsoup.parse(html);
        String leagueName = doc.getElementsByClass("league-name").text();
        String gameType = doc.getElementsByClass("sportitem  checked").text();
        for (Element e : doc.getElementsByClass("gamepanel")) {
            Elements items = e.getElementsByClass("team");
            Game game = Game.builder()
                    .gameType(gameType)
                    .leagueName(leagueName)
                    .firstTeam(items.get(0).text()).secondTeam(items.get(1).text())
                    .id(Long.parseLong(e.attr("gameid")))
                    .build();

            game.setStartTime(e.getElementsByClass("date").text(), e.getElementsByClass("time").text());
            games.add(game);
        }
    }

    private static void parsePrintAndRemoveGame(Game game, String html) {
        Document doc = Jsoup.parse(html);
        System.out.println(game);
        for (Element e : doc.getElementsByClass("betpanel")) {
            System.out.println(e.getElementsByClass("betheader").text());
            for (Element bet : e.getElementsByClass("tip")) {
                String name = bet.getElementsByClass("tiptext").text();
                String value = bet.getElementsByClass("odd").text();
                if (name.length() > 0 && value.length() > 0)
                System.out.println(name + ", " + value);
            }
        }
        games.remove(game);
    }

}
