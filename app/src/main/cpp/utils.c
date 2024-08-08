#include "utils.h"
#include "byedpi/params.h"
#include "main.h"

struct params default_params;

void reset_params(void) {
    clear_params();
    params = default_params;
}
