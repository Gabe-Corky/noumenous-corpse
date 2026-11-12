#include "corpse_primitives.h"

#include <cassert>
#include <cstdint>

int main() {
    using namespace corpse;
    assert(encode_octet(24U, 7U) == 0xC7U);
    assert(extract_fractal(0xC7U) == 24U);
    assert(extract_node(0xC7U) == 7U);
    assert(rotate_left(0xC7U, 1U) == 0x8FU);
    assert(rotate_right(0xC7U, 1U) == 0xE3U);

    std::uint64_t aligned = 0;
    assert(align_down(0x12345U, 4096U, aligned));
    assert(aligned == 0x12000U);
    assert(!align_down(0x12345U, 0U, aligned));

    constexpr std::uint8_t vector[] = {'1','2','3','4','5','6','7','8','9'};
    assert(crc32c(vector, sizeof(vector)) == 0xE3069283U);
    assert(12ULL * 64ULL * 1024ULL * 1024ULL == 768ULL * 1024ULL * 1024ULL);
    return 0;
}
