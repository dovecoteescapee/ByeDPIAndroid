extern char *oob_char;
extern int NOT_EXIT;

struct sockaddr_ina;

int get_default_ttl();

int get_addr(const char *str, struct sockaddr_ina *addr);

void *add(void **root, int *n, size_t ss);

void clear_params(void);

char *ftob(const char *str, ssize_t *sl);

char *data_from_str(const char *str, ssize_t *size);

size_t parse_cform(char *buffer, size_t blen,
                   const char *str, size_t slen);

struct mphdr *parse_hosts(char *buffer, size_t size);

int parse_offset(struct part *part, const char *str);
