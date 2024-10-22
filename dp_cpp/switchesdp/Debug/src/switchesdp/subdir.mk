################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../src/switchesdp/switchesdp.cpp 

OBJS += \
./src/switchesdp/switchesdp.o 

CPP_DEPS += \
./src/switchesdp/switchesdp.d 


# Each subdirectory must supply rules for building sources it contributes
src/switchesdp/%.o: ../src/switchesdp/%.cpp src/switchesdp/subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$@" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


