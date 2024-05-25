@Service
public class NewsService {

    private final NewsRepository newsRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public NewsService(NewsRepository newsRepository, ModelMapper modelMapper) {
        this.newsRepository = newsRepository;
        this.modelMapper = modelMapper;
    }

    public List<NewsDto> getAllActiveNews() {
        List<NewsEntity> newsEntities = newsRepository.findByDeletedFalse();
        return newsEntities.stream()
                .map(newsEntity -> modelMapper.map(newsEntity, NewsDto.class))
                .collect(Collectors.toList());
    }

    public List<NewsDto> getAllNews() {
        List<NewsEntity> newsEntities = newsRepository.findAll();
        return newsEntities.stream()
                .map(newsEntity -> modelMapper.map(newsEntity, NewsDto.class))
                .collect(Collectors.toList());
    }
    
    public void addNews(NewsDto newsDto) {
        NewsEntity newsEntity = modelMapper.map(newsDto, NewsEntity.class);
        newsRepository.save(newsEntity);
    }
    
    public void updateNews(NewsDto newsDto) {
        NewsEntity newsEntity = newsRepository.findById(newsDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("News not found"));
        newsEntity.setTitle(newsDto.getTitle());
        newsEntity.setContent(newsDto.getContent());
        newsRepository.save(newsEntity);
    }
}
