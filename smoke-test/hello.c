#include <statistics.h>
#include <sqlite3.h>

int f() {
    jaf_sum(0, 0);
    sqlite3_initialize();
    return 1;
}
