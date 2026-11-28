/*
   Copyright 2025 Kyant

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.kyant.backdrop.internal

private const val RoundedRectSDF = """
float2 safeNormalize(float2 v) {
    float len = length(v);
    return len > 0.0001 ? v / len : float2(0.0, 0.0);
}

float radiusAt(float2 coord, float4 radii) {
    if (coord.x >= 0.0) {
        if (coord.y <= 0.0) return radii.y;
        else return radii.z;
    } else {
        if (coord.y <= 0.0) return radii.x;
        else return radii.w;
    }
}

float sdRoundedRect(float2 coord, float2 halfSize, float radius) {
    float2 cornerCoord = abs(coord) - (halfSize - float2(radius));
    float outside = length(max(cornerCoord, 0.0)) - radius;
    float inside = min(max(cornerCoord.x, cornerCoord.y), 0.0);
    return outside + inside;
}

float2 gradSdRoundedRect(float2 coord, float2 halfSize, float radius) {
    float2 cornerCoord = abs(coord) - (halfSize - float2(radius));
    if (cornerCoord.x > 0.0 || cornerCoord.y > 0.0) {
        float2 m = max(cornerCoord, 0.0);
        return sign(coord) * safeNormalize(m);
    } else {
        float gradX = step(cornerCoord.y, cornerCoord.x);
        return sign(coord) * float2(gradX, 1.0 - gradX);
    }
}"""

internal const val RoundedRectRefractionShaderString = """
uniform shader content;

uniform float2 size;
uniform float2 offset;
uniform float4 cornerRadii;
uniform float refractionHeight;
uniform float refractionAmount;
uniform float depthEffect;

$RoundedRectSDF

float circleMap(float x) {
    float clampedX = clamp(x, 0.0, 1.0);
    return 1.0 - sqrt(max(0.0, 1.0 - clampedX * clampedX));
}

half4 main(float2 coord) {
    if (size.x <= 0.0 || size.y <= 0.0 || refractionHeight <= 0.0) {
        return content.eval(coord);
    }
    float2 halfSize = size * 0.5;
    float2 centeredCoord = (coord + offset) - halfSize;
    float radius = radiusAt(coord, cornerRadii);
    
    float sd = sdRoundedRect(centeredCoord, halfSize, radius);
    if (-sd >= refractionHeight) {
        return content.eval(coord);
    }
    sd = min(sd, 0.0);
    
    float d = circleMap(1.0 - -sd / max(0.001, refractionHeight)) * refractionAmount;
    float gradRadius = min(radius * 1.5, min(halfSize.x, halfSize.y));
    float2 grad = safeNormalize(gradSdRoundedRect(centeredCoord, halfSize, gradRadius) + depthEffect * safeNormalize(centeredCoord));
    
    float2 refractedCoord = coord + d * grad;
    return content.eval(refractedCoord);
}"""

internal val RoundedRectRefractionWithDispersionShaderString = """
uniform shader content;

uniform float2 size;
uniform float2 offset;
uniform float4 cornerRadii;
uniform float refractionHeight;
uniform float refractionAmount;
uniform float depthEffect;
uniform float chromaticAberration;

$RoundedRectSDF

float circleMap(float x) {
    float clampedX = clamp(x, 0.0, 1.0);
    return 1.0 - sqrt(max(0.0, 1.0 - clampedX * clampedX));
}

half4 main(float2 coord) {
    if (size.x <= 0.0 || size.y <= 0.0 || refractionHeight <= 0.0) {
        return content.eval(coord);
    }
    float2 halfSize = size * 0.5;
    float2 centeredCoord = (coord + offset) - halfSize;
    float radius = radiusAt(coord, cornerRadii);
    
    float sd = sdRoundedRect(centeredCoord, halfSize, radius);
    if (-sd >= refractionHeight) {
        return content.eval(coord);
    }
    sd = min(sd, 0.0);
    
    float d = circleMap(1.0 - -sd / max(0.001, refractionHeight)) * refractionAmount;
    float gradRadius = min(radius * 1.5, min(halfSize.x, halfSize.y));
    float2 grad = safeNormalize(gradSdRoundedRect(centeredCoord, halfSize, gradRadius) + depthEffect * safeNormalize(centeredCoord));
    
    float2 refractedCoord = coord + d * grad;
    float halfArea = max(0.001, halfSize.x * halfSize.y);
    float dispersionIntensity = chromaticAberration * clamp((centeredCoord.x * centeredCoord.y) / halfArea, -2.0, 2.0);
    float2 dispersedCoord = d * grad * dispersionIntensity;
    
    half4 color = half4(0.0);
    
    half4 red = content.eval(refractedCoord + dispersedCoord);
    color.r += red.r / 3.5;
    color.a += red.a / 7.0;
    
    half4 orange = content.eval(refractedCoord + dispersedCoord * (2.0 / 3.0));
    color.r += orange.r / 3.5;
    color.g += orange.g / 7.0;
    color.a += orange.a / 7.0;
    
    half4 yellow = content.eval(refractedCoord + dispersedCoord * (1.0 / 3.0));
    color.r += yellow.r / 3.5;
    color.g += yellow.g / 3.5;
    color.a += yellow.a / 7.0;
    
    half4 green = content.eval(refractedCoord);
    color.g += green.g / 3.5;
    color.a += green.a / 7.0;
    
    half4 cyan = content.eval(refractedCoord - dispersedCoord * (1.0 / 3.0));
    color.g += cyan.g / 3.5;
    color.b += cyan.b / 3.0;
    color.a += cyan.a / 7.0;
    
    half4 blue = content.eval(refractedCoord - dispersedCoord * (2.0 / 3.0));
    color.b += blue.b / 3.0;
    color.a += blue.a / 7.0;
    
    half4 purple = content.eval(refractedCoord - dispersedCoord);
    color.r += purple.r / 7.0;
    color.b += purple.b / 3.0;
    color.a += purple.a / 7.0;
    
    return color;
}"""

internal const val DefaultHighlightShaderString = """
uniform float2 size;
uniform float4 cornerRadii;
layout(color) uniform half4 color;
uniform float angle;
uniform float falloff;

$RoundedRectSDF

half4 main(float2 coord) {
    if (size.x <= 0.0 || size.y <= 0.0) {
        return half4(0.0);
    }
    float2 halfSize = size * 0.5;
    float2 centeredCoord = coord - halfSize;
    float radius = radiusAt(coord, cornerRadii);
    
    float gradRadius = min(radius * 1.5, min(halfSize.x, halfSize.y));
    float2 grad = gradSdRoundedRect(centeredCoord, halfSize, gradRadius);
    float2 normal = float2(cos(angle), sin(angle));
    float d = dot(grad, normal);
    float intensity = pow(abs(d), max(0.1, falloff));
    return color * intensity;
}"""

internal const val AmbientHighlightShaderString = """
uniform float2 size;
uniform float4 cornerRadii;
uniform float angle;
uniform float falloff;

$RoundedRectSDF

half4 main(float2 coord) {
    if (size.x <= 0.0 || size.y <= 0.0) {
        return half4(0.0);
    }
    float2 halfSize = size * 0.5;
    float2 centeredCoord = coord - halfSize;
    float radius = radiusAt(coord, cornerRadii);
    
    float gradRadius = min(radius * 1.5, min(halfSize.x, halfSize.y));
    float2 grad = gradSdRoundedRect(centeredCoord, halfSize, gradRadius);
    float2 normal = float2(cos(angle), sin(angle));
    float d = dot(grad, normal);
    float intensity = pow(abs(d), max(0.1, falloff));
    float t = step(0.0, d);
    return half4(t, t, t, 1.0) * intensity;
}"""

internal const val AdaptiveLegibilityShaderString = """
uniform shader content;

uniform float darkSurface;
uniform float threshold;
uniform float targetLuma;
uniform float strength;
uniform float3 tintRgb;

half4 main(float2 coord) {
    half4 sampled = content.eval(coord);
    float alpha = float(sampled.a);
    if (alpha <= 0.001 || strength <= 0.0001) {
        return sampled;
    }

    float3 rgb = clamp(float3(sampled.rgb) / alpha, 0.0, 1.0);
    float luma = dot(rgb, float3(0.2126, 0.7152, 0.0722));
    float3 adjusted = rgb;
    float clampedStrength = clamp(strength, 0.0, 1.0);

    if (darkSurface > 0.5) {
        float upper = max(threshold + 0.12, 0.88);
        float excess = smoothstep(threshold, upper, luma);
        float factor = excess * clampedStrength;
        if (factor > 0.0001) {
            float safeLuma = max(luma, 0.001);
            float compressedLuma = min(luma, targetLuma);
            float3 scaled = rgb * (compressedLuma / safeLuma);
            float3 smoked = mix(scaled, tintRgb, 0.25);
            adjusted = mix(rgb, smoked, factor);
        }
    } else {
        float lower = min(threshold - 0.12, 0.12);
        float deficit = 1.0 - smoothstep(lower, threshold, luma);
        float factor = deficit * clampedStrength;
        if (factor > 0.0001) {
            float liftedLuma = max(luma, targetLuma);
            float3 lifted = clamp(rgb + float3(liftedLuma - luma), 0.0, 1.0);
            float3 frosted = mix(lifted, tintRgb, 0.22);
            adjusted = mix(rgb, frosted, factor);
        }
    }

    adjusted = clamp(adjusted, 0.0, 1.0);
    return half4(half3(adjusted * alpha), half(alpha));
}"""
