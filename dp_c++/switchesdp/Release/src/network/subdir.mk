################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../src/network/network.cpp 

CPP_DEPS += \
./src/network/network.d 

OBJS += \
./src/network/network.o 


# Each subdirectory must supply rules for building sources it contributes
src/network/%.o: ../src/network/%.cpp src/network/subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -O3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$@" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


clean: clean-src-2f-network

clean-src-2f-network:
	-$(RM) ./src/network/network.d ./src/network/network.o

.PHONY: clean-src-2f-network

