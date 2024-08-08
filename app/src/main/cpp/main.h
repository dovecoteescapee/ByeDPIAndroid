extern char *oob_char;
extern int NOT_EXIT;

struct sockaddr_ina;

int get_default_ttl();

int get_addr(const char *str, struct sockaddr_ina *addr);

void *add(void **root, int *n, size_t ss);

void clear_params(void);
