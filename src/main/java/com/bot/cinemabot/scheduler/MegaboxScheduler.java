package com.bot.cinemabot.scheduler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
//@Component
@RestController
public class MegaboxScheduler {

//	@Autowired
//	private Telegram telegram;

	@Value("${bot.cinema.megabox.api}")
	private String megabox;
	@Value("${bot.cinema.megabox.detailUrl}")
	private String detailUrl;

	@PostConstruct
	public void init() throws IOException {
		getMegaboxTickets();
	}

//	@Scheduled(initialDelay = 1_000, fixedDelayString = "5000")
//	public void aJob() throws IOException {
//		getMegaboxData();
//	}

	@GetMapping("/list")
	private List<Ticket> getMegaboxTickets() throws IOException {
		// desktop 페이지로 파싱함. 추 후 모바일 페이지로 바꾸는게 좋아보임.
		Document megaboxDocument = Jsoup.connect(megabox).get();
		Elements ticketsElements = megaboxDocument.select(".store_lst:nth-child(4) > li");
		return ticketsElements.stream()
				.map(element -> {
					String price = element.select(".price > b").text().trim();
					Elements aTag = element.select("a.blank");
					String name = aTag.attr("title");
					String itemCode = aTag.attr("data-code");
					boolean isSoldOut = !element.select(".store_img_wrap > .tx_soldout").isEmpty();
					return new Ticket(name, price, "", detailUrl + itemCode, isSoldOut);
				})
				.collect(Collectors.toList());
	}

	@GetMapping("/detail/{itemCode}")
	private Ticket getDetailInfo(@PathVariable String itemCode) throws IOException {
		Document megaboxDocument = Jsoup.connect(detailUrl + itemCode).get();
		String date = megaboxDocument.select("#content .good_info .date time").text();
		String price = megaboxDocument.select("#content #displayPrice").val();
		String name = megaboxDocument.select("#form_pay input[name=itemName]").val();
		return new Ticket(name, price, date, detailUrl + itemCode, false);
	}

}

@Data
@AllArgsConstructor
class Ticket {
	private String name;
	private String price;
	private String date;
	private String link;
	private boolean isSoldOut;
}