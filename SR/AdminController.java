@RestController
@RequestMapping("/admin/news")
public class AdminController {

    private final NewsService newsService;

    @Autowired
    public AdminController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping
    public List<NewsDto> getAllNews() {
        return newsService.getAllNews();
    }

    @PostMapping
    public void addNews(@RequestBody NewsDto newsDto) {
        newsService.addNews(newsDto);
    }

    @PutMapping
    public void updateNews(@RequestBody NewsDto newsDto) {
        newsService.updateNews(newsDto);
    }
}
