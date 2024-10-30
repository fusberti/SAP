################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../src/dpswitches/dpswitches.cpp 

CPP_DEPS += \
./src/dpswitches/dpswitches.d 

OBJS += \
./src/dpswitches/dpswitches.o 


# Each subdirectory must supply rules for building sources it contributes
src/dpswitches/%.o: ../src/dpswitches/%.cpp src/dpswitches/subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -O3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$@" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


clean: clean-src-2f-dpswitches

clean-src-2f-dpswitches:
	-$(RM) ./src/dpswitches/dpswitches.d ./src/dpswitches/dpswitches.o

.PHONY: clean-src-2f-dpswitches

