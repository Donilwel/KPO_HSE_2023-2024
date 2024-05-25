@RestController
@RequestMapping("/user/news")
public class NewsController {

    private final NewsService newsService;

    @Autowired
    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping
    public List<NewsDto> getAllActiveNews() {
        return newsService.getAllActiveNews();
    }
}
