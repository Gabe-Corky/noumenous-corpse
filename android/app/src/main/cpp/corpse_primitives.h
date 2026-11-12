#pragma once

#include <cstddef>
#include <cstdint>

namespace corpse {

// Chiasmiarchal byte: five high bits of recursive field, three low node bits.
[[nodiscard]] constexpr std::uint8_t encode_octet(std::uint8_t fractal, std::uint8_t node) noexcept {
    return static_cast<std::uint8_t>(((fractal & 0x1FU) << 3U) | (node & 0x07U));
}

[[nodiscard]] constexpr std::uint8_t extract_fractal(std::uint8_t value) noexcept {
    return static_cast<std::uint8_t>((value & 0xF8U) >> 3U);
}

[[nodiscard]] constexpr std::uint8_t extract_node(std::uint8_t value) noexcept {
    return static_cast<std::uint8_t>(value & 0x07U);
}

[[nodiscard]] constexpr std::uint8_t rotate_left(std::uint8_t value, unsigned shift) noexcept {
    const unsigned amount = shift & 7U;
    if (amount == 0U) return value;
    return static_cast<std::uint8_t>((static_cast<unsigned>(value) << amount) |
                                     (static_cast<unsigned>(value) >> (8U - amount)));
}

[[nodiscard]] constexpr std::uint8_t rotate_right(std::uint8_t value, unsigned shift) noexcept {
    return rotate_left(value, 8U - (shift & 7U));
}

// Round an address down to the actual page boundary supplied by the caller.
// A zero page size is rejected; this is not allocation or memory reservation.
[[nodiscard]] constexpr bool align_down(std::uint64_t address, std::uint64_t page_size,
                                        std::uint64_t& aligned) noexcept {
    if (page_size == 0U) return false;
    aligned = address - (address % page_size);
    return true;
}

// Reflected Castagnoli CRC-32C. Detects accidental corruption; it is not a
// cryptographic hash, authenticity check, or durable-commit protocol.
[[nodiscard]] constexpr std::uint32_t crc32c(const std::uint8_t* data, std::size_t size) noexcept {
    std::uint32_t crc = 0xFFFFFFFFU;
    for (std::size_t i = 0; i < size; ++i) {
        crc ^= data[i];
        for (unsigned bit = 0; bit < 8U; ++bit) {
            const std::uint32_t mask = 0U - (crc & 1U);
            crc = (crc >> 1U) ^ (0x82F63B78U & mask);
        }
    }
    return ~crc;
}

static_assert(encode_octet(24U, 7U) == 0xC7U);
static_assert(extract_fractal(0xC7U) == 24U);
static_assert(extract_node(0xC7U) == 7U);
static_assert(rotate_left(0xC7U, 1U) == 0x8FU);
static_assert(rotate_right(0xC7U, 1U) == 0xE3U);

} // namespace corpse
